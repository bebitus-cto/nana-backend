package com.nana.nana.domain.feature.assemblysync.lpmp

import com.nana.nana.domain.feature.assemblysync.mp.datamodel.MPSyncDataModel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * 관계 데이터는 저장하지 않음
 */
@Service
class SyncTransLPMPService(
    private val syncTransLPMPRepository: SyncTransLPMPRepository
) {

    private val logger: Logger = LoggerFactory.getLogger(SyncTransLPMPService::class.java)

    /**
     * 대표발의자, 공동발의자, 찬성의원 다국어 이름 리스트 가져오기
     */
//    suspend fun selectMPNames(
//        mpIdMap: Map<String, String>,
//        currentAllLawProposalIds: Set<String>,
//        originalData: LPSyncDataModel,
//        languageCode: String
//    ): ParticipantSyncDataModel? {
//        val participants = originalData.participants ?: return null
//
//        // 1️⃣ 국회의원 전체 리스트를 한 번에 조회
//        val allNames = participants.mainProposers.orEmpty() +
//                participants.coProposers.orEmpty() +
//                participants.supporters.orEmpty()
//
//        if (allNames.isEmpty()) {
//            return null
//        }
//
//        // 2️⃣ 언어별 번역 실행 (한 번에 조회)
//        val translatedNames =
//            transLPMPRepository.mapAndSelectTranslatedMPNames(mpIdMap, allNames, languageCode)
//
//        // 3️⃣ 원본 리스트와 번역 리스트를 매핑 (zip 사용)
//        val translatedResults = allNames.zip(translatedNames).toMap()
//
//        return ParticipantSyncDataModel(
//            mainProposers = participants.mainProposers?.map { translatedResults[it] ?: it },
//            coProposers = participants.coProposers?.map { translatedResults[it] ?: it },
//            supporters = participants.supporters?.map { translatedResults[it] ?: it }
//        )
//    }

    /**
     * 이거 맞추기
     */
    suspend fun mapAndTranslateProposerOverview(
        existingMPs: Map<String, MPSyncDataModel>,
        originalOverview: String,
        mainProposers: List<String>,
        targetLanguage: String
    ): String {

        if (mainProposers.isEmpty() || originalOverview.isEmpty()) {
            return ""
        }

        // 🔹 1️⃣ 국회의원 이름을 번역 (다국어 변환)
        val translatedMainProposers =
            syncTransLPMPRepository.mapAndSelectTranslatedMPNames(existingMPs, mainProposers, targetLanguage)

        // 🔹 2️⃣ 기존 `proposerOverview`에서 대표발의자 이름을 번역된 이름으로 교체
        var updatedOverview = originalOverview

        mainProposers.forEachIndexed { index, name ->
            if (index < translatedMainProposers.count()) {
                updatedOverview = updatedOverview.replaceFirst(name, translatedMainProposers[index])
            }
        }

        // 🔹 3️⃣ "등 10인 외 20인" 부분을 변환
        return formatProposerOverview(updatedOverview, targetLanguage)
    }


    fun formatProposerOverview(text: String, targetLanguage: String): String {
        val regex = Regex("(\\d+)인?(등|외)?")  // 숫자 + "인" + "등" 또는 "외" 여부 확인
        return regex.replace(text) { matchResult ->
            val count = matchResult.groupValues[1] // 숫자 부분
            val suffix = matchResult.groupValues[2] // "등" 또는 "외"

            val translatedCount = "$count ${PROPOSER_COUNT_TRANSLATIONS[targetLanguage]?.get("인") ?: "인"}"
            val translatedSuffix = PROPOSER_COUNT_TRANSLATIONS[targetLanguage]?.get(suffix) ?: suffix

            "$translatedCount $translatedSuffix"
        }
    }

    companion object {
        val PROPOSER_COUNT_TRANSLATIONS = mapOf(
            "en" to mapOf(
                "등" to "etc.",
                "외" to "excluding",
                "인" to "members"
            ),
            "zh" to mapOf(
                "등" to "等",
                "외" to "之外",
                "인" to "人"
            ),
            "ja" to mapOf(
                "등" to "等",
                "외" to "を除く",
                "인" to "人"
            )
        )
    }
}