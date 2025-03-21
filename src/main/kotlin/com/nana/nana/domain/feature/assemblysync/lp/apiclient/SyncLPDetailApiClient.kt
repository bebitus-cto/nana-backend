package com.nana.nana.domain.feature.assemblysync.lp.apiclient

import com.nana.nana.config.WebClientConfig.Companion.ASSEMBLY_QUALIFIER
import com.nana.nana.domain.enums.LPMainStatus
import com.nana.nana.domain.enums.LPMainStatus.*
import com.nana.nana.domain.enums.LPSubStatus
import com.nana.nana.domain.enums.LPSubStatus.*
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.Url.LAW_PROPOSAL_FULL_DETAIL_URL
import com.nana.nana.config.ApiConfig.defaultJson
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.getOpenAssemblyBaseUriBuilder
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.toDecodedUriString
import com.nana.nana.domain.feature.assemblysync.apihelper.handleOpenAssemblyApiResponse
import com.nana.nana.domain.feature.assemblysync.lp.datamodel.LPSyncDataModel
import com.nana.nana.domain.feature.assemblysync.lp.datamodel.LPUrlDataModel
import com.nana.nana.domain.feature.assemblysync.lp.response.*
import com.nana.nana.domain.feature.assemblysync.mp.datamodel.MPSyncDataModel
import com.nana.nana.domain.feature.log.LogLPSyncRepository
import com.nana.nana.util.DateParser
import com.nana.nana.util.WebParser
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate

@Component
class SyncLPDetailApiClient(
    @Qualifier(ASSEMBLY_QUALIFIER) private val webClient: WebClient,
    private val syncParticipantsApiClient: SyncParticipantsApiClient,
    private val logLPSyncRepository: LogLPSyncRepository,
    private val webParser: WebParser
) {

    private val logger: Logger = LoggerFactory.getLogger(SyncLPDetailApiClient::class.java)

    suspend fun fetchLPDetail(id: String): LPDetailResponse? = coroutineScope {

        try {
            val url = getOpenAssemblyBaseUriBuilder(LAW_PROPOSAL_FULL_DETAIL_URL)
                .queryParam("BILL_NO", id)
                .toDecodedUriString()

            val body = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String::class.java)
                .awaitSingleOrNull()
                .orEmpty()

            val response = defaultJson.decodeFromString<LPDetailApiResponse>(body)

            response.handleOpenAssemblyApiResponse(
                onSuccess = { _, pagedItems ->

                    val rawResponse = pagedItems?.lastOrNull()

                    return@handleOpenAssemblyApiResponse if (rawResponse == null) {
                        newSuspendedTransaction {
                            logLPSyncRepository.logLPSyncError("의안 [상세정보] 불러오기 실패 아이템 없음", id)
                        }
                        null
                    } else {
                        rawResponse
                    }
                },
                onError = { e ->
                    newSuspendedTransaction {
                        logLPSyncRepository.logLPSyncError("의안 [상세정보] 불러오기 실패 onError: $e", id)
                    }
                    return@handleOpenAssemblyApiResponse null
                }
            )

        } catch (e: Exception) {
            newSuspendedTransaction {
                logLPSyncRepository.logLPSyncError("의안 [최초] 동기화 실패 tryCatch: $e", id)
            }
            null
        }
    }

    /**
     * 최초 삽입 시
     * 이미 있는 데이터와 비교해서 같지 않을때만 가져오기
     */
    suspend fun mapLPDetail(
        rawResponse: LPDetailResponse,
        id: String,
        pendingIds: Set<String>,
        progressLegis: Map<String, ProgressLegisResponse>,
        endedLegis: Map<String, EndedLegisResponse>,
        existigMPs: Map<String, MPSyncDataModel>
    ): LPSyncDataModel? = coroutineScope {

        // 의안의 상태
        val (mainStatus, subStatus) = determineStatus(rawResponse, pendingIds, progressLegis, endedLegis)

        val noticeEndDate = endedLegis[rawResponse.id]?.noticeEndDate

        val previewTexts = webParser.extractPreviewText(rawResponse.id, rawResponse.linkUrl)
        val participants =
            syncParticipantsApiClient.fetchParticipants(rawResponse.id,rawResponse.extraId, rawResponse.proposerKind, existigMPs)

        val proposerOverview = if (participants != null && rawResponse.proposerKind?.contains("의원") == true) {
            participants.proposerOverview
        } else {
            rawResponse.proposerOverview
        }.orEmpty()

        val fileUrlsOnlyRaw = webParser.extractLPFileRawUrls(rawResponse.id, rawResponse.linkUrl)

        return@coroutineScope LPSyncDataModel.fromInitialSync(
            rawResponse = rawResponse,
            noticeEndDate = noticeEndDate,
            previewTexts = previewTexts,
            mainStatus = mainStatus,
            subStatus = subStatus,
            participants = participants,
            proposerOverview = proposerOverview,
            lpUrlDataModel = fileUrlsOnlyRaw,
        )
    }

    /**
     * 추가 동기화 시
     * 이미 있는 데이터와 비교해서 같지 않을때만 가져오기
     */
    suspend fun mapLPDetailForUpdate(
        rawResponse: LPDetailResponse,
        id: String,
        pendingIds: Set<String>,
        progressLegis: Map<String, ProgressLegisResponse>,
        endedLegis: Map<String, EndedLegisResponse>,
        existingLPUrls: Map<String, LPUrlDataModel?>
    ): LPSyncDataModel? = coroutineScope {

        // 의안의 상태
        val (mainStatus, subStatus) = determineStatus(rawResponse, pendingIds, progressLegis, endedLegis)

        val fileUrlsOnlyRaw = existingLPUrls[id]
            ?: webParser.extractLPFileRawUrls(rawResponse.id, rawResponse.linkUrl)

        return@coroutineScope LPSyncDataModel.fromUpdateSync(
            rawResponse = rawResponse,
            mainStatus = mainStatus,
            subStatus = subStatus,
            lpUrlDataModel = fileUrlsOnlyRaw,
        )
    }

    suspend fun determineStatus(
        rawResponse: LPDetailResponse,
        pendingIds: Set<String>,
        progressLegis: Map<String, ProgressLegisResponse>,
        endedLegis: Map<String, EndedLegisResponse>
    ): Pair<LPMainStatus?, LPSubStatus?> = rawResponse.run {
        when {
            // [공포]
            promulgationLawName != null && promulgationDate != null && promulgationNo != null -> {
                PROMULGATION to COMPLETED
            }

            promulgationLawName != null || promulgationDate != null || promulgationNo != null -> {
                if (id in pendingIds) {
                    PROMULGATION to PENDING
                } else {
                    PROMULGATION to IN_PROGRESS
                }
            }

            // [정부 이송]
            governmentTransferDate != null -> {
                if (id in pendingIds) {
                    GOVERNMENT_TRANSFER to PENDING
                } else {
                    GOVERNMENT_TRANSFER to IN_PROGRESS
                }
            }

            // [본회의]
            plenaryResult != null -> {
                PLENARY to plenaryResult.fromResultToSubStatus()
            }

            plenaryPresentationDate != null -> {
                if (id in pendingIds) {
                    PLENARY to PENDING
                } else {
                    PLENARY to UNDER_REVIEW
                }
            }

            // [법사위]
            judicialResult != null -> {
                JUDICIAL to judicialResult.fromResultToSubStatus()
            }

            judicialReferralDate != null || judicialPresentationDate != null -> {
                if (id in pendingIds) {
                    JUDICIAL to PENDING
                } else {
                    JUDICIAL to UNDER_REVIEW
                }
            }

            // [소관위]
            committeeResult != null -> {
                COMMITTEE to committeeResult.fromResultToSubStatus()
            }

            /**
             * [우선조건] 소관위 이상단계에서는 입법예고가 후순위
             * 계류에도 입법예고가 포함 돼있음
             * 소관위부터는 진행중 입법예고, 종료된 입법예고를 제외하고 소관위로 판단하기
             */
            id in endedLegis -> {
                if (hasPassedSevenDaysAfterNoticeEnd(id, endedLegis)) {
                    COMMITTEE to PENDING
                } else {
                    OPEN_FOR_COMMENTS_ENDED to IN_PROGRESS
                }
            }

            id in progressLegis -> {
                OPEN_FOR_COMMENTS_IN_PROGRESS to IN_PROGRESS
            }

            committeePresentationDate != null -> {
                if (id in pendingIds) {
                    COMMITTEE to PENDING
                } else {
                    COMMITTEE to UNDER_REVIEW
                }
            }
            // 2025.03.10 외주 클라이언트 요청으로 임의로 발의단계로 이동
//            committeeCommitDate != null -> {
//                if (id in pendingIds) {
//                    COMMITTEE to PENDING
//                } else {
//                    COMMITTEE to UNDER_REVIEW
//                }
//            }

            else -> {
                if (id in pendingIds) {
                    PROPOSED to PENDING
                } else {
                    PROPOSED to IN_PROGRESS
                }
            }
        }
    }
}

/**
 * 입법예고 종료된 후에도 만약 상임위 결과값이 없다면
 * 강제로 '상임위 계류' 상태로 보내기
 */
private fun hasPassedSevenDaysAfterNoticeEnd(
    targetLPId: String,
    endedLegis: Map<String, EndedLegisResponse>
): Boolean {

    val noticeEndDateStr = endedLegis[targetLPId]?.noticeEndDate
    val noticeEndDate = DateParser.toLocalDate(noticeEndDateStr)

    if (noticeEndDate == null) {
        return false
    } else {
        val currentDate = LocalDate.now()
        return noticeEndDate.plusDays(7)?.isBefore(currentDate) ?: false
    }
}

private fun String.fromResultToSubStatus(): LPSubStatus? =
    listOf(
        APPROVED,
        DISCARDED,
        REJECTED,
        WITHDRAWN
    ).find { contains(it.koValue) }