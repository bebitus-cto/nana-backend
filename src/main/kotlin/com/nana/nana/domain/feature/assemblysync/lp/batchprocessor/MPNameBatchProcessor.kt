package com.nana.nana.domain.feature.assemblysync.lp.batchprocessor

import com.nana.nana.domain.feature.assemblysync.mp.datamodel.MPSyncDataModel
import com.nana.nana.domain.feature.assemblysync.mp.SyncTransMPRepository
import com.nana.nana.domain.feature.translation.config.TransConfig.targetLanguages
import com.nana.nana.domain.feature.translation.openai.GPTRequest
import com.nana.nana.domain.feature.translation.openai.MPNamePrompt.generatePromptForExistingZhOnly
import com.nana.nana.domain.feature.translation.openai.MPNamePrompt.generatePromptForExistingEnAndZh
import com.nana.nana.domain.feature.translation.openai.MPNamePrompt.generatePromptForExistingEnOnly
import com.nana.nana.domain.feature.translation.openai.MPNamePrompt.generatePromptForMissingEnAndZh
import com.nana.nana.domain.feature.translation.openai.MPNamePrompt.gptEnOnlyLanguages
import com.nana.nana.domain.feature.translation.openai.MPNamePrompt.gptExistingEnAndZhLanguages
import com.nana.nana.domain.feature.translation.openai.MPNamePrompt.gptMissingEnAndZhLanguages
import com.nana.nana.domain.feature.translation.openai.MPNamePrompt.gptZhOnlyLanguages
import com.nana.nana.domain.feature.translation.openai.apiclient.GPTGenApiClient
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class MPNameBatchProcessor(
    private val syncTransMPRepository: SyncTransMPRepository,
    private val gptGenApiClient: GPTGenApiClient
) {
    private val logger: Logger = LoggerFactory.getLogger(MPNameBatchProcessor::class.java)
    private val mutex = Mutex()
    private val pendingQueueAllExists = mutableListOf<MPSyncDataModel>()
    private val pendingQueueFull = mutableListOf<MPSyncDataModel>()
    private val pendingQueueEnglishOnly = mutableListOf<MPSyncDataModel>()
    private val pendingQueueChineseOnly = mutableListOf<MPSyncDataModel>()

    suspend fun addMPs(
        newMPs: List<MPSyncDataModel>,
        existingMPs: Map<String, MPSyncDataModel>
    ): Boolean = coroutineScope {

        val batchFullTranslation = mutableListOf<MPSyncDataModel>()  // 영어 & 중국어 둘 다 없는 경우
        val batchAllExists = mutableListOf<MPSyncDataModel>()        // 영어 & 중국어 둘 다 있는 경우
        val batchEnglishOnly = mutableListOf<MPSyncDataModel>()      // 영어만 있는 경우
        val batchChineseOnly = mutableListOf<MPSyncDataModel>()      // 중국어만 있는 경우

        for (newMP in newMPs) {
            val existingMP = existingMPs[newMP.id]

            // ✅ 기존 MP 데이터와 `name` 값이 같다면 번역 요청에서 제외
            if (existingMP != null && existingMP.name == newMP.name) {
                continue
            }

            when {
                newMP.nameInEnglish != null && newMP.nameInChinese != null -> batchAllExists.add(newMP)
                newMP.nameInEnglish == null && newMP.nameInChinese == null -> batchFullTranslation.add(newMP)
                newMP.nameInEnglish != null -> batchEnglishOnly.add(newMP)
                newMP.nameInChinese != null -> batchChineseOnly.add(newMP)
            }
        }

        logger.info("국회에 영어, 중국어 이름이 있는 MPs => ${batchAllExists.count()}")
        logger.info("국회에 영어, 중국어 이름이 둘다 없는 MPs => ${batchFullTranslation.count()}")
        logger.info("국회에 영어 이름만 있는 MPs => ${batchEnglishOnly.count()}")
        logger.info("국회에 중국어 이름만 있는 MPs => ${batchChineseOnly.count()}")

        // 각 그룹별로 processBatch를 비동기로 실행하여 결과(성공 여부)를 Deferred<Boolean>로 모음
        val deferredResults = mutableListOf<Deferred<Boolean>>()

        if (batchFullTranslation.isNotEmpty()) {
            pendingQueueFull.addAll(batchFullTranslation)
            deferredResults.add(async(Dispatchers.IO) {
                processBatch(pendingQueueFull, includeEnglish = false, includeChinese = false) ?: false
            })
        }
        if (batchAllExists.isNotEmpty()) {
            pendingQueueAllExists.addAll(batchAllExists)
            deferredResults.add(async(Dispatchers.IO) {
                processBatch(pendingQueueAllExists, includeEnglish = true, includeChinese = true) ?: false
            })
        }
        if (batchEnglishOnly.isNotEmpty()) {
            pendingQueueEnglishOnly.addAll(batchEnglishOnly)
            deferredResults.add(async(Dispatchers.IO) {
                processBatch(pendingQueueEnglishOnly, includeEnglish = true, includeChinese = false) ?: false
            })
        }
        if (batchChineseOnly.isNotEmpty()) {
            pendingQueueChineseOnly.addAll(batchChineseOnly)
            deferredResults.add(async(Dispatchers.IO) {
                processBatch(pendingQueueChineseOnly, includeEnglish = false, includeChinese = true) ?: false
            })
        }

        // 모든 비동기 작업 결과를 기다린 후, 모두 성공하면 true, 하나라도 실패하면 false 반환
        val results = deferredResults.awaitAll()
        return@coroutineScope results.all { it }
    }

    private suspend fun processBatch(
        rawBatch: MutableList<MPSyncDataModel>,
        includeEnglish: Boolean,
        includeChinese: Boolean
    ): Boolean? {

        val batch: List<MPSyncDataModel>
        mutex.withLock {
            if (rawBatch.isEmpty()) {
                return null
            }
            batch = rawBatch.toList()
            rawBatch.clear()
        }

        // 기존 데이터 보존 (영어 & 중국어가 있으면 저장)
        val existingResults = batch.associateBy({ it.id }) { mp ->
            mutableMapOf<String, String>().apply {
                mp.nameInEnglish?.let { put("en", it) }  // 기존 영어 이름이 있다면 저장
                mp.nameInChinese?.let { put("zh", it) }  // 기존 중국어 이름이 있다면 저장
            }.toMap()
        }

        logger.info("🚀 GPT 번역 요청 - 총 요청할 개수: ${batch.count()}")

        val semaphore = Semaphore(10)
        val chunkedRequests = batch.chunked(10)
        val deferredResults = coroutineScope {
            chunkedRequests.map { chunk ->
                async(Dispatchers.IO) {
                    semaphore.withPermit {
                        processSubBatch(chunk, includeEnglish, includeChinese)
                    }
                }
            }
        }

        val gptResultsList = deferredResults.awaitAll()
        logger.info("gptResultsList: $gptResultsList")

        val (gptResults, isSuccess) = gptResultsList
            .fold(mutableMapOf<String, Map<String, String>>() to true)
            { (acc, flag), map ->
                acc.putAll(map)
                Pair(acc, flag && map.isNotEmpty())
            }

        val finalResults = mergeWithExistingData(gptResults, existingResults)
        return syncTransMPRepository.batchUpsertMPNames(finalResults) && isSuccess

    }

    private suspend fun processSubBatch(
        batch: List<MPSyncDataModel>,
        includeEnglish: Boolean, // 영어 번역 포함 여부
        includeChinese: Boolean  // 중국어 번역 포함 여부
    ): Map<String, Map<String, String>> {

        // ✅ GPT 요청을 만들기 위해 사용할 이름 리스트 생성
        val userContent =
            batch.joinToString(separator = "\n") { mp -> if (includeEnglish) mp.nameInEnglish!! else mp.name }

        val (systemPrompt, modifiedTargetLanguages) = when {
            includeEnglish && includeChinese -> generatePromptForExistingEnAndZh() to gptExistingEnAndZhLanguages
            !includeEnglish && !includeChinese -> generatePromptForMissingEnAndZh() to gptMissingEnAndZhLanguages
            includeEnglish && !includeChinese -> generatePromptForExistingEnOnly() to gptEnOnlyLanguages
            !includeEnglish && includeChinese -> generatePromptForExistingZhOnly() to gptZhOnlyLanguages
            else -> null
        } ?: return run {
            logger.warn("알 수 없는 프롬프트 조건: $userContent")
            emptyMap()
        }

        val gptRequest = GPTRequest(
            messages = listOf(
                mapOf("role" to "system", "content" to systemPrompt),
                mapOf("role" to "user", "content" to userContent)
            )
        )

        return gptGenApiClient.batchTranslateMPnamesMultiLangExcludingKo(
            batch, gptRequest, modifiedTargetLanguages
        )
    }

    private fun mergeWithExistingData( // 기존 데이터 + GPT 결과 병합
        gptTranslatedMap: Map<String, Map<String, String>>,
        existingTranslationMap: Map<String, Map<String, String>>
    ): Map<String, Map<String, String?>> {
        return gptTranslatedMap.mapValues { (mpId, gptTranslations) ->
            val existingNames = existingTranslationMap.entries.find { it.key == mpId }?.value ?: emptyMap()

            targetLanguages.associateWith { targetLanguage ->
                when {
                    existingNames.containsKey("en") && existingNames.containsKey("zh") -> {
                        existingNames[targetLanguage] ?: gptTranslations[targetLanguage]
                    }

                    existingNames.containsKey("en") && existingNames.containsKey("zh") -> {
                        gptTranslations[targetLanguage]  // GPT 번역 결과 그대로 사용
                    }

                    existingNames.containsKey("en") && !existingNames.containsKey("zh") -> {
                        if (targetLanguage == "en") existingNames["en"] else gptTranslations[targetLanguage]
                    }

                    !existingNames.containsKey("en") && existingNames.containsKey("zh") -> {
                        if (targetLanguage == "zh") existingNames["zh"] else gptTranslations[targetLanguage]
                    }
                    // ✅ 기본적으로 GPT 결과를 사용 (예외 방지)
                    else -> gptTranslations[targetLanguage]
                }
            }
        }
    }

    suspend fun retryAddMPs(
        failedMPs: List<MPSyncDataModel>
    ): Boolean = coroutineScope {
        // 각 MP를 번역 데이터 존재 여부에 따라 네 그룹으로 분리합니다.
        val batchFullTranslation = mutableListOf<MPSyncDataModel>()   // 영어, 중국어 둘 다 없는 경우
        val batchAllExists = mutableListOf<MPSyncDataModel>()         // 영어, 중국어 둘 다 있는 경우
        val batchEnglishOnly = mutableListOf<MPSyncDataModel>()       // 영어만 있는 경우
        val batchChineseOnly = mutableListOf<MPSyncDataModel>()       // 중국어만 있는 경우

        for (mp in failedMPs) {
            when {
                mp.nameInEnglish != null && mp.nameInChinese != null -> batchAllExists.add(mp)
                mp.nameInEnglish == null && mp.nameInChinese == null -> batchFullTranslation.add(mp)
                mp.nameInEnglish != null -> batchEnglishOnly.add(mp)
                mp.nameInChinese != null -> batchChineseOnly.add(mp)
            }
        }

        logger.info("국회에 영어, 중국어 이름이 있는 MPs => ${batchAllExists.count()}")
        logger.info("국회에 영어, 중국어 이름이 둘다 없는 MPs => ${batchFullTranslation.count()}")
        logger.info("국회에 영어 이름만 있는 MPs => ${batchEnglishOnly.count()}")
        logger.info("국회에 중국어 이름만 있는 MPs => ${batchChineseOnly.count()}")

        // 각 그룹별로 processBatch를 비동기로 실행하여 결과를 Deferred<Boolean>로 모읍니다.
        val deferredResults = mutableListOf<Deferred<Boolean>>()

        if (batchFullTranslation.isNotEmpty()) {
            pendingQueueFull.addAll(batchFullTranslation)
            deferredResults.add(async {
                processBatch(pendingQueueFull, includeEnglish = false, includeChinese = false) ?: false
            })
        }
        if (batchAllExists.isNotEmpty()) {
            pendingQueueAllExists.addAll(batchAllExists)
            deferredResults.add(async {
                processBatch(pendingQueueAllExists, includeEnglish = true, includeChinese = true) ?: false
            })
        }
        if (batchEnglishOnly.isNotEmpty()) {
            pendingQueueEnglishOnly.addAll(batchEnglishOnly)
            deferredResults.add(async {
                processBatch(pendingQueueEnglishOnly, includeEnglish = true, includeChinese = false) ?: false
            })
        }
        if (batchChineseOnly.isNotEmpty()) {
            pendingQueueChineseOnly.addAll(batchChineseOnly)
            deferredResults.add(async {
                processBatch(pendingQueueChineseOnly, includeEnglish = false, includeChinese = true) ?: false
            })
        }
        // 모든 배치 작업 결과를 기다리고, 모두 성공했는지 확인합니다.
        val results = deferredResults.awaitAll()
        return@coroutineScope results.all { it }
    }
}