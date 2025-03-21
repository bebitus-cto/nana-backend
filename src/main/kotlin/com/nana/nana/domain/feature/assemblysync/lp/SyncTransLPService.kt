package com.nana.nana.domain.feature.assemblysync.lp

import com.nana.nana.domain.feature.assemblysync.lp.apiclient.TransApiClient
import com.nana.nana.domain.feature.assemblysync.lp.datamodel.LPSyncDataModel
import com.nana.nana.domain.feature.assemblysync.lpmp.SyncTransLPMPService
import com.nana.nana.domain.feature.assemblysync.mp.datamodel.MPSyncDataModel
import com.nana.nana.domain.feature.translation.config.TransConfig.targetLanguages
import com.nana.nana.util.WebParser
import com.nana.nana.util.WebParser.PreviewTextTitleType.Companion.PREVIEW_CONTENT_CLAUSE_TRANSLATIONS
import com.nana.nana.util.WebParser.PreviewTextTitleType.Companion.getTranslatedTitle
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SyncTransLPService(
    private val syncTransLPRepository: SyncTransLPRepository,
    private val syncTransLPMPService: SyncTransLPMPService,
    private val transApiClient: TransApiClient
) {

    private val logger: Logger = LoggerFactory.getLogger(SyncTransLPService::class.java)

    suspend fun batchTranslateLPsDetail(
        existingMPs: Map<String, MPSyncDataModel>,
        newLPs: List<LPSyncDataModel>,
        existingLPs: Map<String, LPSyncDataModel>
    ): List<Pair<String, List<Map<String, Any?>>>> = coroutineScope {
        logger.info("법안 상세 정보 번역 배치작업 시작: 신규:${newLPs.count()}건, 기존:${existingLPs.count()}건")

        val semaphore = Semaphore(5)
        val chunkedNewLPs = newLPs.chunked(5)

        val deferredTranslations = coroutineScope {
            chunkedNewLPs.map { chunk ->
                async(Dispatchers.IO) {
                    chunk.map { newLP ->
                        semaphore.withPermit {
                            val existingLP = existingLPs[newLP.id]
                            val diffMap: MutableMap<String, String?> = if (existingLP != null) {
                                getChangedFields(newLP, existingLP)
                            } else {
                                getChangedFields(newLP)
                            }.toMutableMap()

                            // 의원 발의가 아닐 때만 번역 요청
                            if (existingLP == null || newLP.proposerOverview != existingLP.proposerOverview) {
                                if (newLP.participants == null) {
                                    diffMap["proposerOverview"] = newLP.proposerOverview
                                }
                            }

                            val keysToTranslate = diffMap.keys.toList()
                            val textsToTranslate = keysToTranslate.map { diffMap[it].orEmpty() }
                            val translationResults = mutableListOf<Pair<String, Map<String, Any?>>>()

                            if (textsToTranslate.isNotEmpty()) {
                                // 1. 한국어 → 영어 번역 (영어 번역 결과를 캐싱)
                                val englishTexts =
                                    transApiClient.requestTranslation(newLP.id, textsToTranslate, "ko", "en")
                                if (englishTexts.isNotEmpty() && englishTexts.count() == textsToTranslate.count()) {
                                    val englishTranslatedMap = keysToTranslate.zip(englishTexts) { key, translation ->
                                        key to translation.ifBlank { null }
                                    }.toMap()

                                    val updateMapEnglish = mutableMapOf<String, Any?>().apply {
                                        putAll(englishTranslatedMap)

                                        // 번역이 필요 없는 필드들
                                        put("id", newLP.id.withLanguageCode("en"))
                                        put("extraId", newLP.extraId)

                                        // 신규 or 개정
                                        put("isNew", newLP.isNew)

                                        if (existingLP == null || newLP.proposalDate != existingLP.proposalDate) {
                                            put("proposalDate", newLP.proposalDate)
                                        }
                                        if (existingLP == null || newLP.noticeEndDate != existingLP.noticeEndDate) {
                                            put("noticeEndDate", newLP.noticeEndDate)
                                        }
                                        if (existingLP == null || newLP.committeeName != existingLP.committeeName) {
                                            put("committeeName", newLP.committeeName)
                                        }
                                        if (existingLP == null || newLP.committeeResult != existingLP.committeeResult) {
                                            put("committeeResult", newLP.committeeResult)
                                        }
                                        if (existingLP == null || newLP.judicialResult != existingLP.judicialResult) {
                                            put("judicialResult", newLP.judicialResult)
                                        }
                                        if (existingLP == null || newLP.plenaryResult != existingLP.plenaryResult) {
                                            put("plenaryResult", newLP.plenaryResult)
                                        }
                                        if (existingLP == null || newLP.governmentTransferDate != existingLP.governmentTransferDate) {
                                            put("governmentTransferDate", newLP.governmentTransferDate)
                                        }
                                        if (existingLP == null || newLP.promulgationDate != existingLP.promulgationDate) {
                                            put("promulgationDate", newLP.promulgationDate)
                                        }
                                        if (existingLP == null || newLP.mainStatus != existingLP.mainStatus) {
                                            put("mainStatus", newLP.mainStatus)
                                        }
                                        if (existingLP == null || newLP.subStatus != existingLP.subStatus) {
                                            put("subStatus", newLP.subStatus)
                                        }
                                        if (existingLP == null || newLP.participants != existingLP.participants) {
                                            if (newLP.participants != null) {

                                                // 🔹 참여자가 있으면 대표발의자 이름을 번역 후, 후행 숫자 표현 변환
                                                val translatedProposerOverview =
                                                    syncTransLPMPService.mapAndTranslateProposerOverview(
                                                        existingMPs,
                                                        newLP.proposerOverview.orEmpty(),
                                                        newLP.participants.mainProposers.orEmpty(),
                                                        "en"
                                                    )
                                                if (englishTranslatedMap["proposerOverview"] == null) {
                                                    put("proposerOverview", translatedProposerOverview)
                                                }
                                            }
                                        }
                                    }
                                    translationResults.add("en" to updateMapEnglish)

                                    // 2. 영어 번역 결과를 소스로 하여, 영어를 제외한 다른 targetLanguage로 번역 요청
                                    targetLanguages.drop(1).forEach { targetLanguage ->
                                        val textsFromEnglish =
                                            keysToTranslate.map { englishTranslatedMap[it].orEmpty() }
                                        val translatedTexts =
                                            transApiClient.requestTranslation(
                                                newLP.id,
                                                textsFromEnglish,
                                                "en",
                                                targetLanguage
                                            )

                                        if (translatedTexts.isNotEmpty() && translatedTexts.count() == keysToTranslate.count()) {
                                            val translatedMap =
                                                keysToTranslate.zip(translatedTexts) { key, translation ->
                                                    key to translation.ifBlank { null }
                                                }.toMap()

                                            val updateMap = mutableMapOf<String, Any?>().apply {
                                                putAll(translatedMap)

                                                // 번역되지 않지만 유지해야 할 필드들 추가
                                                put("id", newLP.id.withLanguageCode(targetLanguage))
                                                put("extraId", newLP.extraId)

                                                if (existingLP == null || newLP.proposalDate != existingLP.proposalDate) {
                                                    put("proposalDate", newLP.proposalDate)
                                                }
                                                if (existingLP == null || newLP.noticeEndDate != existingLP.noticeEndDate) {
                                                    put("noticeEndDate", newLP.noticeEndDate)
                                                }
                                                if (existingLP == null || newLP.committeeName != existingLP.committeeName) {
                                                    put("committeeName", newLP.committeeName)
                                                }
                                                if (existingLP == null || newLP.committeeResult != existingLP.committeeResult) {
                                                    put("committeeResult", newLP.committeeResult)
                                                }
                                                if (existingLP == null || newLP.judicialResult != existingLP.judicialResult) {
                                                    put("judicialResult", newLP.judicialResult)
                                                }
                                                if (existingLP == null || newLP.plenaryResult != existingLP.plenaryResult) {
                                                    put("plenaryResult", newLP.plenaryResult)
                                                }
                                                if (existingLP == null || newLP.governmentTransferDate != existingLP.governmentTransferDate) {
                                                    put("governmentTransferDate", newLP.governmentTransferDate)
                                                }
                                                if (existingLP == null || newLP.promulgationDate != existingLP.promulgationDate) {
                                                    put("promulgationDate", newLP.promulgationDate)
                                                }
                                                if (existingLP == null || newLP.mainStatus != existingLP.mainStatus) {
                                                    put("mainStatus", newLP.mainStatus)
                                                }
                                                if (existingLP == null || newLP.subStatus != existingLP.subStatus) {
                                                    put("subStatus", newLP.subStatus)
                                                }
                                                if (existingLP == null || newLP.participants != existingLP.participants) {
                                                    if (newLP.participants != null) {

                                                        // 🔹 참여자가 있으면 대표발의자 이름을 번역 후, 후행 숫자 표현 변환
                                                        val translatedProposerOverview =
                                                            syncTransLPMPService.mapAndTranslateProposerOverview(
                                                                existingMPs,
                                                                newLP.proposerOverview.orEmpty(),
                                                                newLP.participants.mainProposers.orEmpty(),
                                                                targetLanguage
                                                            )
                                                        if (englishTranslatedMap["proposerOverview"] == null) {
                                                            put("proposerOverview", translatedProposerOverview)
                                                        }
                                                    }
                                                }
                                            }
                                            translationResults.add(targetLanguage to updateMap)
                                        }
                                    }
                                }
                            }

                            if (existingLP == null || newLP.previewTexts != existingLP.previewTexts) {
                                val previewTranslations: Map<String, List<WebParser.LawProposalPreviewText>> =
                                    translatePreviewTexts(
                                        lpId = newLP.id,
                                        previewTexts = newLP.previewTexts ?: emptyList(),
                                        targetLanguages = targetLanguages
                                    )

                                translationResults.map { (language, updateMap) ->
                                    language to updateMap.toMutableMap().apply {
                                        put("previewTexts", previewTranslations[language])
                                    }
                                }
                            } else {
                                translationResults
                            }
                        }
                    }.flatten()
                }
            }
        }

        val allTranslations: List<Pair<String, Map<String, Any?>>> = deferredTranslations.awaitAll().flatten()
        val grouped: Map<String, List<Map<String, Any?>>> = allTranslations.groupBy({ it.first }, { it.second })
        grouped.map { (lang, updates) -> lang to updates }
    }

    private suspend fun translatePreviewTexts(
        lpId: String,
        previewTexts: List<WebParser.LawProposalPreviewText>,
        targetLanguages: List<String>,
        chunkLimit: Int = 3000
    ): Map<String, List<WebParser.LawProposalPreviewText>> = coroutineScope {

        // 1. 각 previewText의 각 컨텐츠에서 원래 접두어와 본문(접두어 제거된 부분)을 추출하여 저장
        val previewTextPrefixes = mutableListOf<String>() // 원래 접두어 ("가.", "나." 등)
        val previewTextBodies = mutableListOf<String>()     // 접두어 제거된 본문
        previewTexts.forEach { preview ->
            preview.contents.forEach { content ->
                val regex = Regex("^[가-하]\\.")
                val matchResult = regex.find(content)
                val originalPrefix = matchResult?.value.orEmpty()
                val body = content.removePrefix(originalPrefix).trim()
                previewTextPrefixes.add(originalPrefix)
                previewTextBodies.add(body)
            }
        }

        // 2. 텍스트들을 청크 단위로 분리하는 헬퍼 함수
        fun chunkTexts(texts: List<String>, limit: Int): List<List<String>> {
            val chunks = mutableListOf<List<String>>()
            var currentChunk = mutableListOf<String>()
            var currentLength = 0
            texts.forEach { text ->
                if (currentLength + text.length > limit && currentChunk.isNotEmpty()) {
                    chunks.add(currentChunk.toList())
                    currentChunk = mutableListOf(text)
                    currentLength = text.length
                } else {
                    currentChunk.add(text)
                    currentLength += text.length
                }
            }
            if (currentChunk.isNotEmpty()) chunks.add(currentChunk)
            return chunks
        }

        // 3. 한국어 → 영어 번역 (본문 부분, 청크 단위, 비동기 병렬 처리)
        val originalChunks = chunkTexts(previewTextBodies, chunkLimit)
        val englishTranslations = originalChunks.map { chunk ->
            async { transApiClient.requestTranslation(lpId, chunk, "ko", "en") }
        }.awaitAll().flatten()
        // 영어 번역 결과를 인덱스와 매핑
        val englishTranslationMap = (0 until previewTextBodies.size).associateWith { index ->
            englishTranslations.getOrElse(index) { "" }
        }

        // 4. 영어 번역 결과를 기반으로 다른 언어 번역 (청크 단위, 비동기 병렬 처리)
        val languageTranslationMaps = mutableMapOf<String, Map<Int, String>>()
        languageTranslationMaps["en"] = englishTranslationMap

        val otherLanguages = targetLanguages.filter { it != "en" }
        val deferredOtherTranslations = otherLanguages.map { lang ->
            async {
                // 영어 번역 결과 목록(본문 부분)
                val englishTexts = (0 until previewTextBodies.size).map { index ->
                    englishTranslationMap[index].orEmpty()
                }
                val englishChunksForLang = chunkTexts(englishTexts, chunkLimit)
                val langTranslations = englishChunksForLang.map { chunk ->
                    async { transApiClient.requestTranslation(lpId, chunk, "en", lang) }
                }.awaitAll().flatten()
                lang to (0 until previewTextBodies.size).associateWith { index ->
                    langTranslations.getOrElse(index) { "" }
                }
            }
        }
        deferredOtherTranslations.awaitAll().forEach { (lang, map) ->
            languageTranslationMaps[lang] = map
        }

        // 5. 원본 previewTexts 구조로 재조립
        val result = mutableMapOf<String, MutableList<WebParser.LawProposalPreviewText>>()
        targetLanguages.forEach { lang ->
            result[lang] = mutableListOf()
        }
        var index = 0
        previewTexts.forEach { preview ->
            val contentCount = preview.contents.size
            targetLanguages.forEach { lang ->
                val translatedContents = mutableListOf<String>()
                repeat(contentCount) {
                    // 원래 저장한 접두어를 사용하여 번역된 접두어 lookup
                    val originalPrefix = previewTextPrefixes.getOrElse(index) { "" }
                    val translatedPrefix = PREVIEW_CONTENT_CLAUSE_TRANSLATIONS[lang]?.get(originalPrefix).orEmpty()
                    val translatedBody = languageTranslationMaps[lang]?.get(index).orEmpty()
                    val combined =
                        if (translatedPrefix.isNotEmpty()) "$translatedPrefix $translatedBody" else translatedBody
                    translatedContents.add(combined.trim())
                    index++
                }
                val newPreview = WebParser.LawProposalPreviewText(
                    titleType = preview.titleType,
                    translatedTitle = preview.titleType.getTranslatedTitle(lang),
                    contents = translatedContents
                )
                result[lang]?.add(newPreview)
            }
        }
        result.mapValues { it.value.toList() }
    }

    fun getChangedFields(
        newLP: LPSyncDataModel,
        existingLP: LPSyncDataModel? = null
    ): Map<String, String?> {
        val diffMap = linkedMapOf<String, String?>()

        if (existingLP == null) {
            // 기존 데이터가 없을 경우 모든 필드를 추가
            diffMap["kind"] = newLP.kind
            diffMap["name"] = newLP.name
            diffMap["proposerKind"] = newLP.proposerKind
            diffMap["proposerOverview"] = newLP.proposerOverview
            diffMap["proposeSession"] = newLP.proposeSession
            diffMap["committeeName"] = newLP.committeeName
            diffMap["plenaryConferenceName"] = newLP.plenaryConferenceName
            diffMap["promulgationLawName"] = newLP.promulgationLawName
            return diffMap
        }

        // 기존 데이터가 있을 경우 비교 후 변경된 항목만 추가
        if (newLP.name != existingLP.name) {
            diffMap["name"] = newLP.name
        }
        if (newLP.kind != existingLP.kind) {
            diffMap["kind"] = newLP.kind
        }
        if (newLP.proposerKind != existingLP.proposerKind) {
            diffMap["proposerKind"] = newLP.proposerKind
        }
        if (newLP.proposerOverview != existingLP.proposerOverview) {
            diffMap["proposerOverview"] = newLP.proposerOverview
        }
        if (newLP.proposeSession != existingLP.proposeSession) {
            diffMap["proposeSession"] = newLP.proposeSession
        }
        if (newLP.committeeName != existingLP.committeeName) {
            diffMap["committeeName"] = newLP.committeeName
        }
        if (newLP.plenaryConferenceName != existingLP.plenaryConferenceName) {
            diffMap["plenaryConferenceName"] = newLP.plenaryConferenceName
        }
        if (newLP.promulgationLawName != existingLP.promulgationLawName) {
            diffMap["promulgationLawName"] = newLP.promulgationLawName
        }

        return diffMap
    }

    suspend fun batchUpsertLPsDetail(translatedLPs: List<Pair<String, List<Map<String, Any?>>>>) {
        syncTransLPRepository.batchUpsertLPsDetail(translatedLPs)
    }
}