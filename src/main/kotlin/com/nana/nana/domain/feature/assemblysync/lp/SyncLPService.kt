package com.nana.nana.domain.feature.assemblysync.lp

import com.nana.nana.domain.enums.LPSubStatus
import com.nana.nana.domain.feature.assemblysync.config.SyncConfig.CURRENT_MP_ERACO
import com.nana.nana.domain.feature.assemblysync.lp.apiclient.SyncLPDetailApiClient
import com.nana.nana.domain.feature.assemblysync.lp.apiclient.SyncLPLegisApiClient
import com.nana.nana.domain.feature.assemblysync.lp.apiclient.SyncLPOverviewApiClient
import com.nana.nana.domain.feature.assemblysync.lp.apiclient.SyncLPPendingApiClient
import com.nana.nana.domain.feature.assemblysync.lp.batchprocessor.LPFileBatchProcessor
import com.nana.nana.domain.feature.assemblysync.lp.batchprocessor.NicknameBatchProcessor
import com.nana.nana.domain.feature.assemblysync.lp.datamodel.LPSyncDataModel
import com.nana.nana.domain.feature.assemblysync.lp.datamodel.LPUrlDataModel
import com.nana.nana.domain.feature.assemblysync.lp.response.EndedLegisResponse
import com.nana.nana.domain.feature.assemblysync.lp.response.ProgressLegisResponse
import com.nana.nana.domain.feature.assemblysync.lpmp.SyncLPMPRepository
import com.nana.nana.domain.feature.assemblysync.mp.SyncMPRepository
import com.nana.nana.domain.feature.assemblysync.mp.SyncTransMPService
import com.nana.nana.domain.feature.assemblysync.mp.datamodel.MPSyncDataModel
import com.nana.nana.domain.feature.log.LogLPSyncRepository
import com.nana.nana.table.lpmp.LPsMPsTable
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import kotlin.math.pow

@Service
class SyncLPService(
    private val syncLPRepository: SyncLPRepository,
    private val syncLPLegisApiClient: SyncLPLegisApiClient,
    private val syncLPPendingApiClient: SyncLPPendingApiClient,
    private val syncLPDetailApiClient: SyncLPDetailApiClient,
    private val syncLPOverviewApiClient: SyncLPOverviewApiClient,
    private val syncLPUrlRepository: SyncLPUrlRepository,
    private val syncLPMPRepository: SyncLPMPRepository,
    private val syncMPRepository: SyncMPRepository,
    private val nicknameBatchProcessor: NicknameBatchProcessor,
    private val lpFileBatchProcessor: LPFileBatchProcessor,
    private val applicationScope: CoroutineScope,
    private val logLPSyncRepository: LogLPSyncRepository,
    private val syncTransLPService: SyncTransLPService,
) {

    private val logger: Logger = LoggerFactory.getLogger(SyncLPService::class.java)
    private val semaphore = Semaphore(10)

    @Scheduled(cron = "0 30 3 * * *", zone = "Asia/Seoul", scheduler = "lpTaskScheduler")
    fun syncLPs() = applicationScope.launch {
        val syncNewContext = fetchNewSyncContext() ?: return@launch
        val syncExistingContext = fetchExistingSyncContext() ?: return@launch

        val alreadySyncIds = syncInitial(syncNewContext, syncExistingContext)
        syncUpdate(alreadySyncIds, syncNewContext, syncExistingContext)
    }

    suspend fun syncInitial(
        syncNewContext: LPNewSyncContext,
        syncExistingContext: LPExistingContext
    ): Set<String> {
        val (newProgressLegis, newEndedLegis, newPendingIds, newCurrentEracoIds) = syncNewContext
        val (existingLPs, existingLPIds, existingLPUrls, existingMPs) = syncExistingContext


        val translatedLPs =
            syncTransLPService.batchTranslateLPsDetail(
                existingMPs,
                existingLPs.values.sortedBy { it.id.toInt() }.toList().take(100),
                emptyMap()
            )
        return emptySet()
        syncTransLPService.batchUpsertLPsDetail(translatedLPs)

        val targetIds = newCurrentEracoIds - existingLPIds

        logger.info("targetIds: ${targetIds.count()}")

        val fetchedLPs = coroutineScope {
            targetIds.chunked(10).flatMap { chunk ->
                chunk.map { id ->
                    async(Dispatchers.IO) {
                        semaphore.withPermit {
                            fetchLPDetail(id)?.let { rawResponse ->
                                syncLPDetailApiClient.mapLPDetail(
                                    rawResponse, id, newPendingIds, newProgressLegis, newEndedLegis, existingMPs
                                )
                            }
                        }
                    }
                }
            }
        }.awaitAll().filterNotNull()

        return newSuspendedTransaction {

            val fetchedLPIds = fetchedLPs.map { it.id }.toSet()
            val fetchedLPRawUrls = fetchedLPs.map { it.lpUrlDataModel }
            val failedIds = targetIds - fetchedLPIds

            logLPSyncRepository.logLPSyncError("현 ${CURRENT_MP_ERACO}대 의안 최초동기화 요청: [${failedIds.count()}]개 실패", "")

            syncLPRepository.batchUpsertLPsDetail(fetchedLPIds, fetchedLPs)

            syncLPMPRepository.batchInsertIgnoreLPsMPs(fetchedLPIds, fetchedLPs, existingMPs)

            val partyRanksMap = syncLPMPRepository.getLPMainPartiesIfChanged(fetchedLPs, existingLPs)
            syncLPRepository.batchUpsertLPleadingParty(partyRanksMap)

            // 파일 raw url
            syncLPUrlRepository.batchUpsertOnlyRawUrls(fetchedLPIds, fetchedLPRawUrls)

            // 파일
            lpFileBatchProcessor.add(fetchedLPRawUrls, existingLPUrls)

            // 닉네임
            nicknameBatchProcessor.add(fetchedLPs, existingLPs)

            fetchedLPIds
        }
    }

    suspend fun syncUpdate(
        alreadySyncIds: Set<String>,
        syncNewContext: LPNewSyncContext,
        syncExistingContext: LPExistingContext
    ) {
        val (newProgressLegis, newEndedLegis, newPendingIds, newCurrentEracoIds) = syncNewContext
        val (_, _, existingLPUrls, _) = syncExistingContext

        val newProgressLegisIds = newProgressLegis.keys
        val newEndedLegisIds = newEndedLegis.keys

        // DB에 있는 의안 중 업데이트가 필요한 의안
        val (existingLPForUpdateIds, existingLPsForUpdate) = newSuspendedTransaction {
            syncLPRepository.selectLPsOnlyNeededUpdate()
        }.let { lp -> lp.keys to lp }

        val existingPendingIds = existingLPsForUpdate.filterValues { it.subStatus == LPSubStatus.PENDING }.keys
        val existingNoPendingIds = existingLPForUpdateIds - existingPendingIds

        // 서버에서 가져온 계류 중 db계류에 없는 것
        val newPendingForUpdateIds = newPendingIds - (newProgressLegisIds + newEndedLegisIds) - existingPendingIds

        // 서버에서 가저온 계류가 아닌 것 중 db의 계류에 있는 것
        val noLongerPendingIds = (newCurrentEracoIds - newPendingIds).intersect(existingPendingIds)

        val updateIds = existingNoPendingIds + newPendingForUpdateIds + noLongerPendingIds - alreadySyncIds

        logger.info("existingNoPendingIds: ${existingNoPendingIds.count()}")
        logger.info("newPendingForUpdateIds: ${newPendingForUpdateIds.count()}")
        logger.info("noLongerPendingIds: ${noLongerPendingIds.count()}")
        logger.info("updateIds: ${updateIds.count()}")

        val fetchedLPs = coroutineScope {
            updateIds.chunked(10).flatMap { chunk ->
                chunk.map { id ->
                    async(Dispatchers.IO) {
                        semaphore.withPermit {
                            fetchLPDetail(id)?.let { rawResponse ->
                                syncLPDetailApiClient.mapLPDetailForUpdate(
                                    rawResponse, id, newPendingIds, newProgressLegis, newEndedLegis, existingLPUrls
                                )
                            }
                        }
                    }
                }
            }
        }.awaitAll().filterNotNull()

        val fetchedLPIds = fetchedLPs.map { it.id }.toSet()
        val fetchedLPRawUrls = fetchedLPs.map { it.lpUrlDataModel }

        logger.warn("현 ${CURRENT_MP_ERACO}대 추가 동기화: ${updateIds - fetchedLPIds}개 실패, ${fetchedLPIds.count()}개 성공")

        newSuspendedTransaction {

            syncLPRepository.batchUpsertLPsDetailOnlyStatus(fetchedLPIds, fetchedLPs)

            // 파일 raw url
            syncLPUrlRepository.batchUpsertOnlyRawUrls(fetchedLPIds, fetchedLPRawUrls)

            // 파일
            lpFileBatchProcessor.add(fetchedLPRawUrls, existingLPUrls)
        }
    }

//    @Scheduled(cron = "0 00 4 * * *", zone = "Asia/Seoul", scheduler = "lpTaskScheduler")
//    fun syncLeftOffLPsNickName() = applicationScope.launch {
//        newSuspendedTransaction {
//            val leftOffLPs = syncLPRepository.fetchLeftoffNicknameLPs()
//            nicknameBatchProcessor.add(leftOffLPs, emptyMap())
//        }
//    }

//    fun syncLeftOffLPsLeadingParty() = applicationScope.launch {
//        newSuspendedTransaction {
//            val leftOffLPs = syncLPRepository.fetchLeftoffLeadingPartyLPs()
//            val ids = leftOffLPs.map { it.id }
//            val count = LPsMPsTable
//                .select { LPsMPsTable.lpId inList ids }
//                .count()
//            logger.debug("카운트: $count")
//            val partyRanksMap = syncLPMPRepository.getLPMainPartiesIfChanged(leftOffLPs, emptyMap())
//            syncLPRepository.batchUpsertLPleadingParty(partyRanksMap)
//        }
//    }

    private suspend fun fetchLPDetail(id: String) =
        runCatching {
            syncLPDetailApiClient.fetchLPDetail(id)
        }.getOrElse { e ->
            newSuspendedTransaction {
                logLPSyncRepository.logLPSyncError("의안 상세 정보 불러오기 실패 getOrElse: $e", id)
            }
            null
        }

    private suspend fun fetchNewSyncContext(): LPNewSyncContext? {

        var attempt = 0
        var lpNewSyncContext: LPNewSyncContext? = null

        while (attempt < 3 && lpNewSyncContext == null) {
            try {

                // 진행중 입법예고(의안 검색에서 조회 불가능)
                val newProgressLegis = syncLPLegisApiClient.fetchProgressLegis()

                // 종료된 입법예고
                val newEndedLegis = syncLPLegisApiClient.fetchEndedLegis()

                // 계류
                val newPendings = syncLPPendingApiClient.fetchPendings()
                val newPendingIds = newPendings.keys

                // 모든 의안(대수)
                val newCurrentEracoIds = syncLPOverviewApiClient.generateLPIdSet()

                // 계류 의안들에 대해서 데이터 삽입?
                logger.info("진행중 입법예고: ${newProgressLegis.count()}, 종료된 입법예고: ${newEndedLegis.count()}, 계류: ${newPendingIds.count()}")
                println("------------------------------------")
                logger.info("진행중 입법예고 <=> 종료된 입법예고: ${(newEndedLegis.keys.intersect(newProgressLegis.keys)).count()}")
                logger.info("계류 <=> 진행중 입법예고: ${(newPendingIds.intersect(newProgressLegis.keys)).count()}")
                logger.info("계류 <=> 종료된 입법예고: ${(newPendingIds.intersect(newEndedLegis.keys)).count()}")
                println("------------------------------------")
                logger.info("총 의안 - (진행중, 종료된 입법예고) 의안 수: ${(newCurrentEracoIds - (newProgressLegis.keys + newEndedLegis.keys)).count()}")
                logger.info("총 의안 - (진행중, 종료된 입법예고) 중 계류에 없는 것: ${((newCurrentEracoIds - (newProgressLegis.keys + newEndedLegis.keys)) - newPendingIds).count()}")
                println("------------------------------------")
                logger.info("현재 대수 의안 수: ${newCurrentEracoIds.count()}")

                lpNewSyncContext = LPNewSyncContext(
                    newProgressLegis = newProgressLegis,
                    newEndedLegis = newEndedLegis,
                    newPendingIds = newPendingIds,
                    newCurrentEracoIds = newCurrentEracoIds,
                )

            } catch (e: Exception) {
                attempt++
                if (attempt >= 3) {
                    newSuspendedTransaction {
                        logLPSyncRepository.logLPSyncError(
                            "의안 ${CURRENT_MP_ERACO}대 정보 불러오기 실패 fetchNewSyncContext",
                            ""
                        )
                    }
                } else {
                    val delayTime = 3000L * (3.0.pow(attempt.toDouble())).toLong()
                    delay(delayTime)
                }
            }
        }

        return lpNewSyncContext
    }

    private suspend fun fetchExistingSyncContext(): LPExistingContext? {

        var attempt = 0
        var lpExistingContext: LPExistingContext? = null

        while (attempt < 3 && lpExistingContext == null) {
            try {

                lpExistingContext = newSuspendedTransaction {
                    val existingLPs = syncLPRepository.selectLPsByEracoAndIds()
                    val existingLPIds = existingLPs.keys
                    val existingLPUrls = existingLPs.mapValues { it.value.lpUrlDataModel }
                    val existingMPs = syncMPRepository.selectAllMPsByNameMap()

                    LPExistingContext(
                        existingLPs = existingLPs,
                        existingLPIds = existingLPIds,
                        existingLPUrls = existingLPUrls,
                        existingMPs = existingMPs
                    )
                }

            } catch (e: Exception) {
                attempt++
                if (attempt >= 3) {
                    newSuspendedTransaction {
                        logLPSyncRepository
                            .logLPSyncError("의안 ${CURRENT_MP_ERACO}대 정보 불러오기 실패 fetchExistingSyncContext", "")
                    }
                } else {
                    val delayTime = 3000L * (3.0.pow(attempt.toDouble())).toLong()
                    delay(delayTime)
                }
            }
        }


        return lpExistingContext
    }


    data class LPNewSyncContext(
        val newProgressLegis: Map<String, ProgressLegisResponse>,
        val newEndedLegis: Map<String, EndedLegisResponse>,
        val newPendingIds: Set<String>,
        val newCurrentEracoIds: Set<String>,
    )

    data class LPExistingContext(
        val existingLPs: Map<String, LPSyncDataModel>,
        val existingLPIds: Set<String>,
        val existingLPUrls: Map<String, LPUrlDataModel?>,
        val existingMPs: Map<String, MPSyncDataModel>
    )
}

