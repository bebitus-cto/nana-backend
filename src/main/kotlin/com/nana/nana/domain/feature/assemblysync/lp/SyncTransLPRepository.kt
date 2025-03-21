package com.nana.nana.domain.feature.assemblysync.lp

import com.nana.nana.domain.enums.LPMainStatus
import com.nana.nana.domain.enums.LPStageResult
import com.nana.nana.domain.enums.LPSubStatus
import com.nana.nana.config.ApiConfig.defaultJson
import com.nana.nana.domain.feature.assemblysync.lp.datamodel.LPSyncDataModel
import com.nana.nana.table.lp.LPsEnTable.nickname
import com.nana.nana.table.lp.getLPsTable
import com.nana.nana.util.WebParser
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.select
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class SyncTransLPRepository {

    private val logger: Logger = LoggerFactory.getLogger(SyncTransLPRepository::class.java)

    fun batchUpsertLPsDetail(translatedLPs: List<Pair<String, List<Map<String, Any?>>>>) {
        translatedLPs.forEach { (targetLanguage, updates) ->
            val translateTable = getLPsTable(targetLanguage)

            val resultRows = translateTable.batchUpsert(updates) { updateMap ->
                this[translateTable.id] = updateMap["id"] as String
                logger.warn("의안아이디: ${updateMap["id"] as String}")
                this[translateTable.extraId] = updateMap["extraId"] as String

                // ✅ 번역된 텍스트 필드들 (문자열 필드)
                updateMap["name"]?.let {
                    this[translateTable.name] = it as String
                }
                updateMap["kind"]?.let {
                    this[translateTable.kind] = it as String
                }
                updateMap["proposerKind"]?.let {
                    this[translateTable.proposerKind] = it as String
                }
                updateMap["proposerOverview"]?.let {
                    this[translateTable.proposerOverview] = it as String
                }
                updateMap["proposeSession"]?.let {
                    this[translateTable.proposeSession] = it as String
                }
                updateMap["committeeName"]?.let {
                    this[translateTable.committeeName] = it as String
                }
                updateMap["plenaryConferenceName"]?.let {
                    this[translateTable.plenaryConferenceName] = it as String
                }
                updateMap["promulgationLawName"]?.let {
                    this[translateTable.promulgationLawName] = it as String
                }

                // ✅ 번역이 필요 없는 ENUM, 날짜, 숫자 Boolean 필드들
                updateMap["isNew"]?.let {
                    this[translateTable.isNew] = it as Boolean
                }
                updateMap["committeeResult"]?.let {
                    this[translateTable.committeeResult] = it as LPStageResult?
                }
                updateMap["judicialResult"]?.let {
                    this[translateTable.judicialResult] = it as LPStageResult?
                }
                updateMap["plenaryResult"]?.let {
                    this[translateTable.plenaryResult] = it as LPStageResult?
                }
                updateMap["mainStatus"]?.let {
                    this[translateTable.mainStatus] = it as LPMainStatus?
                }
                updateMap["subStatus"]?.let {
                    this[translateTable.subStatus] = it as LPSubStatus?
                }

                updateMap["proposalDate"]?.let {
                    this[translateTable.proposalDate] = it as LocalDate?
                }
                updateMap["noticeEndDate"]?.let {
                    this[translateTable.noticeEndDate] = it as LocalDate?
                }
                updateMap["committeeCommitDate"]?.let {
                    this[translateTable.committeeCommitDate] = it as LocalDate?
                }
                updateMap["committeePresentationDate"]?.let {
                    this[translateTable.committeePresentationDate] = it as LocalDate?
                }
                updateMap["committeeProcessingDate"]?.let {
                    this[translateTable.committeeProcessingDate] = it as LocalDate?
                }
                updateMap["judicialReferralDate"]?.let {
                    this[translateTable.judicialReferralDate] = it as LocalDate?
                }
                updateMap["judicialPresentationDate"]?.let {
                    this[translateTable.judicialPresentationDate] = it as LocalDate?
                }
                updateMap["judicialProcessingDate"]?.let {
                    this[translateTable.judicialProcessingDate] = it as LocalDate?
                }
                updateMap["plenaryPresentationDate"]?.let {
                    this[translateTable.plenaryPresentationDate] = it as LocalDate?
                }
                updateMap["plenaryResolutionDate"]?.let {
                    this[translateTable.plenaryResolutionDate] = it as LocalDate?
                }
                updateMap["governmentTransferDate"]?.let {
                    this[translateTable.governmentTransferDate] = it as LocalDate?
                }
                updateMap["promulgationDate"]?.let {
                    this[translateTable.promulgationDate] = it as LocalDate?
                }
                updateMap["promulgationNo"]?.let {
                    this[translateTable.promulgationNo] = it as String
                }

                // ✅ `previewTexts` 번역 필드
                updateMap["previewTexts"]?.let {
                    val previewTexts = it as List<WebParser.LawProposalPreviewText>?
                    logger.debug("프리뷰디버깅 db1: ${previewTexts?.map { it.contents }}")
                    logger.debug(
                        "프리뷰디버깅 db2: ${
                            defaultJson.encodeToString<List<WebParser.LawProposalPreviewText>>(
                                previewTexts.orEmpty()
                            )
                        }"
                    )
                    this[translateTable.previewTexts] =
                        defaultJson.encodeToString<List<WebParser.LawProposalPreviewText>>(previewTexts.orEmpty())
                }
            }

            logger.info("✅ 다국어 의안 정보 batch upsert 완료! (${resultRows.count()}개 처리됨)")
        }
    }

    /**
     * 닉네임이 이미 존재하는지 검사 (한국어 전용)
     */
    fun isTranslatedNicknameExists(translatedNickname: String, targetLanguage: String): Boolean {
        val translationTable = getLPsTable(targetLanguage)

        return translationTable
            .select { nickname eq translatedNickname }
            .empty().not()
    }

    fun batchUpsertLPNicknames(multiLangNicknames: Map<LPSyncDataModel, Map<String, String>>) {
        if (multiLangNicknames.isEmpty()) {
            logger.info("📌 업로드할 다국어 닉네임 데이터가 없음")
            return
        }

        try {
            targetLanguages.forEach { languageCode ->
                val translateTable = getLPsTable(languageCode)

                val updates = multiLangNicknames.mapNotNull { (lp, translations) ->
                    val translatedNickname = translations[languageCode] ?: return@mapNotNull null
                    lp.copy(id = lp.id.withLanguageCode(languageCode)) to translatedNickname
                }

                if (updates.isNotEmpty()) {
                    translateTable.batchUpsert(updates) { (lp, nickname) ->
                        this[translateTable.id] = lp.id
                        this[translateTable.extraId] = lp.extraId
                        this[translateTable.proposerKind] = lp.proposerKind.orEmpty()
                        this[translateTable.proposerOverview] = lp.proposerOverview.orEmpty()
                        this[translateTable.proposeSession] = lp.proposeSession.orEmpty()
                        this[translateTable.nickname] = nickname
                    }
                }
            }

            logger.info("✅ 다국어 닉네임 batch upsert 완료! (${multiLangNicknames.count()}개 법률안 처리됨)")
        } catch (e: Exception) {
            logger.error("❌ 다국어 닉네임 upsert 오류: ${e.message}", e)
        }
    }

}