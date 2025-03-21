package com.nana.nana.domain.feature.assemblysync.lp.batchprocessor

import com.nana.nana.domain.feature.assemblysync.lp.SyncLPRepository
import com.nana.nana.domain.feature.assemblysync.lp.SyncTransLPRepository
import com.nana.nana.domain.feature.assemblysync.lp.datamodel.LPSyncDataModel
import com.nana.nana.domain.feature.translation.config.TransConfig.allLanguages
import com.nana.nana.domain.feature.translation.openai.GPTRequest
import com.nana.nana.domain.feature.translation.openai.LPNicknamePrompt.generatePromptMultiLangNicknamesPrompt
import com.nana.nana.domain.feature.translation.openai.apiclient.GPTGenApiClient
import com.nana.nana.table.lp.getLPsTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import org.jetbrains.exposed.sql.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.system.measureTimeMillis

@Component
class NicknameBatchProcessor(
    private val syncTransLPRepository: SyncTransLPRepository,
    private val syncLPRepository: SyncLPRepository,
    private val gptGenApiClient: GPTGenApiClient,
) {
    private val logger: Logger = LoggerFactory.getLogger(NicknameBatchProcessor::class.java)
    private val mutex = Mutex()
    private val pendingQueue = mutableListOf<LPSyncDataModel>()
    private val semaphore = Semaphore(5)

    suspend fun add(
        newLPs: List<LPSyncDataModel>,
        existingLPs: Map<String, LPSyncDataModel>
    ) {
        val newProposals = newLPs.filter { newLP ->
            val existingLP = existingLPs[newLP.id]
            existingLP == null || existingLP.nickname.isNullOrEmpty() || existingLP.name != newLP.name
        }

        if (newProposals.isNotEmpty()) {
            pendingQueue.addAll(newProposals)
            processBatch()
        }
    }

    private suspend fun processBatch() {
        val batch = mutex.withLock {
            if (pendingQueue.isEmpty()) return
            pendingQueue.toList().also { pendingQueue.clear() }
        }

        logger.info("🚀 GPT 닉네임 번역 요청 시작 - 총 요청 개수: ${batch.size}")

        val deferredResults = coroutineScope {
            batch.chunked(10).mapIndexed { index, chunk ->
                async(Dispatchers.IO) {
                    semaphore.withPermit {
                        try {
                            logger.info("🔄 [$index] 서브 배치 실행 중 (크기: ${chunk.size})")
                            processSubBatch(chunk)
                        } finally {
                            logger.info("🔓 [$index] Semaphore 해제 - 서브 배치 완료")
                        }
                    }
                }
            }
        }

        logger.info("⏳ 모든 GPT 요청 완료 대기 중...")
        val gptResultsList = deferredResults.awaitAll()
        logger.info("✅ 모든 GPT 요청 완료!")

        val gptResults = gptResultsList.fold(mutableMapOf<String, Map<String, String>>()) { acc, map ->
            acc.apply { putAll(map) }
        }

        try {
            logger.info("✅ 닉네임 번역 결과값: $gptResults")
            val multiLangNicknames = addNicknameNumber(batch, gptResults, allLanguages)

            val koreanNicknames = multiLangNicknames.mapValues { it.value["ko"].orEmpty() }
            if (koreanNicknames.isNotEmpty()) {
                logger.debug("한국어 의안이름들: $koreanNicknames")
                syncLPRepository.batchUpsertLPNicknames(koreanNicknames)
            }

            if (multiLangNicknames.isNotEmpty()) {
                syncTransLPRepository.batchUpsertLPNicknames(multiLangNicknames)
            }

        } catch (e: Exception) {
            logger.error("❌ GPT 닉네임 번역 데이터 삽입 오류: ${e.message}", e)
        }
    }

    private suspend fun processSubBatch(
        batch: List<LPSyncDataModel>
    ): Map<String, Map<String, String>> {
        logger.info("🔍 GPT 요청 시작 - 배치 크기: ${batch.size}")

        val userContent = batch.joinToString(separator = "\n") { it.name }

        val gptRequest = GPTRequest(
            messages = listOf(
                mapOf("role" to "system", "content" to generatePromptMultiLangNicknamesPrompt()),
                mapOf("role" to "user", "content" to userContent)
            )
        )

        var result: Map<String, Map<String, String>> = emptyMap()

        val timeTaken = measureTimeMillis {
            result = gptGenApiClient.batchTranslateLPNicknamesMultiLang(batch, gptRequest, allLanguages)
        }

        logger.info("✅ GPT 요청 완료 - 배치 크기: ${batch.size}, 소요 시간: ${timeTaken}ms")

        // 🚨 GPT 응답 디버깅 로그 추가
        result.forEach { (id, translations) ->
            logger.info("📝 GPT 응답 결과 [ID: $id] → $translations")

            if (translations.values.all { it.matches(Regex("^[0-9]+$")) }) {
                logger.error("❌ GPT 응답이 숫자로만 이루어짐! [ID: $id] → $translations")
            }
        }

        return result
    }

    suspend fun addNicknameNumber(
        batch: List<LPSyncDataModel>,
        gptResults: Map<String, Map<String, String>>,
        languages: List<String>
    ): Map<LPSyncDataModel, Map<String, String>> {
        val finalResults = mutableMapOf<LPSyncDataModel, MutableMap<String, String>>()
        batch.forEach { lp -> finalResults[lp] = mutableMapOf() }

        languages.forEach { lang ->
            val lpsTable = getLPsTable(lang)
            val nicknames: Map<LPSyncDataModel, String> = batch.associateWith { lp ->
                gptResults[lp.id]?.get(lang).orEmpty()
            }

            val groupedByNickname = nicknames.entries.groupBy { it.value }
            val baseNicknames = groupedByNickname.keys.toList()

            val conditions: Op<Boolean> = SqlExpressionBuilder.run {
                baseNicknames.fold(Op.FALSE as Op<Boolean>) { acc, base ->
                    acc or (lpsTable.nickname.lowerCase() like "${base.lowercase()}%")
                }
            }

            val existingNicknames = lpsTable
                .slice(lpsTable.nickname)
                .select { conditions }
                .mapNotNull { it[lpsTable.nickname] }

            fun extractNumberSuffix(nickname: String): Int? {
                val matchResult = Regex("([0-9]+)$").find(nickname)
                return matchResult?.value?.toIntOrNull()
            }

            val baseToMaxNumber: Map<String, Int> = baseNicknames.associateWith { base ->
                existingNicknames.filter { it.startsWith(base) }
                    .mapNotNull { extractNumberSuffix(it) }
                    .maxOrNull() ?: 0
            }

            val finalNicknamesForLang = mutableMapOf<LPSyncDataModel, String>()
            for ((baseNickname, entries) in groupedByNickname) {
                val maxNumber = baseToMaxNumber[baseNickname] ?: 0
                entries.forEachIndexed { index, entry ->
                    val finalNickname = if (maxNumber == 0 && index == 0) {
                        baseNickname
                    } else {
                        "$baseNickname${maxNumber + index + 1}"
                    }
                    finalNicknamesForLang[entry.key] = finalNickname
                }
            }

            finalNicknamesForLang.forEach { (lp, finalNick) ->
                finalResults[lp]?.put(lang, finalNick)
            }
        }

        return finalResults
    }
}