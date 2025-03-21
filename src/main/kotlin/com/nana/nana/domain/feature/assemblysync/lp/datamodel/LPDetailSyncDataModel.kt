package com.nana.nana.domain.feature.assemblysync.lp.datamodel

import com.nana.nana.domain.enums.LPMainStatus
import com.nana.nana.domain.enums.LPStageResult
import com.nana.nana.domain.enums.LPSubStatus
import com.nana.nana.domain.feature.assemblysync.lp.response.LPDetailResponse
import com.nana.nana.domain.feature.assemblysync.lpmp.ParticipantSyncDataModel
import com.nana.nana.util.DateParser
import com.nana.nana.util.WebParser.LawProposalPreviewText
import java.time.LocalDate

data class LPSyncDataModel(
    val id: String,
    val extraId: String,
    val kind: String,
    val name: String,
    val isNew: Boolean?,
    val nickname: String? = null,
    val proposerKind: String? = null,
    val proposerOverview: String? = null,
    val proposeSession: String? = null,
    val leadingParty: String? = null, // 위원장 등 대표발의인 경우는 어떻게?
    val proposalDate: LocalDate? = null,
    val noticeEndDate: LocalDate? = null, // 게시 종료일
    val previewTexts: List<LawProposalPreviewText>? = null, // 제안이유 및 주요내용, 제안이유, 주요내용, 참고사항
    // 소관위
    val committeeName: String? = null,
    val committeeCommitDate: LocalDate? = null,
    val committeePresentationDate: LocalDate? = null,
    val committeeProcessingDate: LocalDate? = null,
    val committeeResult: LPStageResult? = null,
    // 법사위
    val judicialReferralDate: LocalDate? = null,
    val judicialPresentationDate: LocalDate? = null,
    val judicialProcessingDate: LocalDate? = null,
    val judicialResult: LPStageResult? = null,
    // 본회의
    val plenaryPresentationDate: LocalDate? = null,
    val plenaryResolutionDate: LocalDate? = null,
    val plenaryConferenceName: String? = null,
    val plenaryResult: LPStageResult? = null,
    val governmentTransferDate: LocalDate? = null,
    val promulgationLawName: String? = null,
    val promulgationDate: LocalDate? = null,
    val promulgationNo: String? = null,
    val mainStatus: LPMainStatus? = null,
    val subStatus: LPSubStatus? = null,
    val participants: ParticipantSyncDataModel? = null,
    val lpUrlDataModel: LPUrlDataModel,
) {
    companion object {

        fun fromInitialSync(
            rawResponse: LPDetailResponse,
            noticeEndDate: String?,
            previewTexts: List<LawProposalPreviewText>?,
            mainStatus: LPMainStatus?,
            subStatus: LPSubStatus?,
            participants: ParticipantSyncDataModel?,
            proposerOverview: String,
            lpUrlDataModel: LPUrlDataModel,
        ): LPSyncDataModel = with(rawResponse) {
            LPSyncDataModel(
                id = id,
                extraId = extraId,
                kind = kind,
                name = name,
                isNew = determineIsNewLP(name),
                proposerKind = proposerKind.orEmpty(),
                proposerOverview = proposerOverview,
                proposeSession = proposeSession.orEmpty(),
                proposalDate = DateParser.toLocalDate(proposalDate),
                noticeEndDate = DateParser.toLocalDate(noticeEndDate),
                committeeName = committeeName,
                committeeCommitDate = DateParser.toLocalDate(committeeCommitDate),
                committeePresentationDate = DateParser.toLocalDate(committeePresentationDate),
                committeeProcessingDate = DateParser.toLocalDate(committeeProcessingDate),
                committeeResult = LPStageResult.toResult(committeeResult),
                judicialReferralDate = DateParser.toLocalDate(judicialReferralDate),
                judicialPresentationDate = DateParser.toLocalDate(judicialPresentationDate),
                judicialProcessingDate = DateParser.toLocalDate(judicialProcessingDate),
                judicialResult = LPStageResult.toResult(judicialResult),
                plenaryPresentationDate = DateParser.toLocalDate(plenaryPresentationDate),
                plenaryResolutionDate = DateParser.toLocalDate(plenaryResolutionDate),
                plenaryConferenceName = plenaryConferenceName,
                plenaryResult = LPStageResult.toResult(plenaryResult),
                governmentTransferDate = DateParser.toLocalDate(governmentTransferDate),
                promulgationLawName = promulgationLawName,
                promulgationDate = DateParser.toLocalDate(promulgationDate),
                promulgationNo = promulgationNo,
                mainStatus = mainStatus,
                subStatus = subStatus,
                participants = participants,
                lpUrlDataModel = lpUrlDataModel,
                previewTexts = previewTexts
            )
        }

        fun fromUpdateSync(
            rawResponse: LPDetailResponse,
            mainStatus: LPMainStatus?,
            subStatus: LPSubStatus?,
            lpUrlDataModel: LPUrlDataModel,
        ): LPSyncDataModel {
            return with(rawResponse) {
                LPSyncDataModel(
                    id = id,
                    extraId = extraId,
                    kind = kind,
                    name = name,
                    isNew = determineIsNewLP(name),
                    committeeName = committeeName,
                    committeeCommitDate = DateParser.toLocalDate(committeeCommitDate),
                    committeePresentationDate = DateParser.toLocalDate(committeePresentationDate),
                    committeeProcessingDate = DateParser.toLocalDate(committeeProcessingDate),
                    committeeResult = LPStageResult.toResult(committeeResult),
                    judicialReferralDate = DateParser.toLocalDate(judicialReferralDate),
                    judicialPresentationDate = DateParser.toLocalDate(judicialPresentationDate),
                    judicialProcessingDate = DateParser.toLocalDate(judicialProcessingDate),
                    judicialResult = LPStageResult.toResult(judicialResult),
                    plenaryPresentationDate = DateParser.toLocalDate(plenaryPresentationDate),
                    plenaryResolutionDate = DateParser.toLocalDate(plenaryResolutionDate),
                    plenaryConferenceName = plenaryConferenceName,
                    plenaryResult = LPStageResult.toResult(plenaryResult),
                    governmentTransferDate = DateParser.toLocalDate(governmentTransferDate),
                    promulgationLawName = promulgationLawName,
                    promulgationDate = DateParser.toLocalDate(promulgationDate),
                    promulgationNo = promulgationNo,
                    mainStatus = mainStatus,
                    subStatus = subStatus,
                    lpUrlDataModel = lpUrlDataModel
                )
            }
        }

        private fun determineIsNewLP(name: String): Boolean = name.endsWith("개정법률안")

    }
}

data class LPUrlDataModel(
    val lpId: String? = null,
    val linkUrl: String? = null,
    val rawPDFUrl: String? = null,
    val rawHWPUrl: String? = null,
    val gcpPDFUrl: String? = null,
    val gcpHWPUrl: String? = null
)