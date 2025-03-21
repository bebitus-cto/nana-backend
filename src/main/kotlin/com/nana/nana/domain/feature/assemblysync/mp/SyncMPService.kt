package com.nana.nana.domain.feature.assemblysync.mp

import com.nana.nana.domain.feature.assemblysync.config.SyncConfig.CURRENT_MP_ERACO
import com.nana.nana.domain.feature.assemblysync.lp.batchprocessor.MPNameBatchProcessor
import com.nana.nana.domain.feature.assemblysync.mp.apiclient.json.SyncResourcePathConverter.getFileFromPastMPsJsonPath
import com.nana.nana.domain.feature.assemblysync.mp.apiclient.SyncMPApiClient
import com.nana.nana.domain.feature.assemblysync.mp.apiclient.MPsJsonDownloader.getCurrentMPsJson
import com.nana.nana.domain.feature.assemblysync.mp.apiclient.MPsJsonDownloader.getCurrentMPsSnsJson
import com.nana.nana.domain.feature.translation.config.TransConfig.allLanguages
import com.nana.nana.table.mp.getMPsTable
import com.nana.nana.domain.feature.translation.openai.GPTSyncRetryService
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class SyncMPService(
    private val syncMPRepository: SyncMPRepository,
    private val syncMpApiClient: SyncMPApiClient,
    private val applciationScope: CoroutineScope,
    private val mpNameBatchProcessor: MPNameBatchProcessor,
    private val gptSyncRetryService: GPTSyncRetryService,
    private val syncTransMPService: SyncTransMPService,
) {

    private val logger: Logger = LoggerFactory.getLogger(SyncMPService::class.java)

    fun syncAllPastMPs() = applciationScope.launch {
        logger.info("📌 [역대 국회의원] 동기화 시작")

        try {
            val pastMPsJson = getFileFromPastMPsJsonPath()
            val chunkedJsonMPs = pastMPsJson.chunked(10)

            measureTimeMillis {
                val semaphore = Semaphore(10)
                val mpDataModels = coroutineScope {
                    chunkedJsonMPs.flatMap { chunk ->
                        chunk.map { mpResp ->
                            async(Dispatchers.IO) {
                                semaphore.withPermit {
                                    runCatching {
                                        syncMpApiClient.fetchMPDetail(mpResp)
                                    }.getOrElse { e ->
                                        logger.error("❌ [역대 국회의원] 상세정보 가져오기 실패 이름: ${mpResp.name}]: ${e.message}")
                                        null
                                    }
                                }
                            }
                        }.awaitAll().filterNotNull()
                    }
                }

                logger.info("✅ [역대 국회의원] ${mpDataModels.count()}명 상세정보 불러오기 완료")


                newSuspendedTransaction {

                    val mpIds = mpDataModels.map { it.id }.toSet()
                    val existingMPs = syncMPRepository.selectMPsByIdsMap(mpIds)

                    /*과거 국회의원은들은 sns 정보 없음*/
                    val newMPs = syncMPRepository.batchUpsertMPsDetail(mpIds, mpDataModels, emptyMap())
                    syncTransMPService.batchTranslateMPsDetail(newMPs, existingMPs)

                    val isSuccess = mpNameBatchProcessor.addMPs(newMPs, existingMPs)
                    if (!isSuccess) {
                        gptSyncRetryService.retryFailedMPBatch()
                    }
                }
            }.let { totalTime ->
                logger.info("====   역대 국회의원 ${pastMPsJson.count()}명 전체 동기화 처리 시간 => ${totalTime / 1000}초   ====")
            }
        } catch (e: Exception) {
            logger.error("❌ [역대 국회의원] 전체 동기화 중 오류 발생: ${e.message}", e)
        }
    }

    @Scheduled(cron = "0 30 3 1 * ?", zone = "Asia/Seoul", scheduler = "mpTaskScheduler")
    fun syncAllCurrentMPs() = applciationScope.launch {
        logger.info("📌 [현 국회의원] 매월 1일 동기화 시작")

        try {
            val jsonMPs = getCurrentMPsJson()
            val jsonMPsSns = getCurrentMPsSnsJson()
            val snsMap = jsonMPsSns.associateBy { it.name }

            measureTimeMillis {
                val semaphore = Semaphore(10)
                val chunkedJsonMPs = jsonMPs.chunked(10)
                val mpDataModels = coroutineScope {
                    chunkedJsonMPs.flatMap { chunk ->  // ✅ 청크 단위로 실행
                        chunk.map { mpResp ->
                            async(Dispatchers.IO) {
                                semaphore.withPermit {
                                    runCatching {
                                        syncMpApiClient.fetchMPDetail(mpResp)
                                    }.getOrElse { e ->
                                        logger.error(
                                            "현 국회의원 상세정보 가져오기 실패 [이름: ${mpResp.name}]: ${e.message}",
                                            e
                                        )
                                        null
                                    }
                                }
                            }
                        }.awaitAll().filterNotNull()
                    }
                }

                logger.info("현 ${CURRENT_MP_ERACO}대 국회의원 ${mpDataModels.count()}명 상세정보 불러오기 완료")

                newSuspendedTransaction {

                    val mpIds = mpDataModels.map { it.id }.toSet()
                    val existingMPs = syncMPRepository.selectMPsByIdsMap(mpIds)

                    val newMPs = syncMPRepository.batchUpsertMPsDetail(mpIds, mpDataModels, snsMap)
                    syncTransMPService.batchTranslateMPsDetail(newMPs, existingMPs)

                    val isSuccess = mpNameBatchProcessor.addMPs(newMPs, existingMPs)
                    if (!isSuccess) {
                        gptSyncRetryService.retryFailedMPBatch()
                    }

                }
            }.let { totalTime ->
                logger.info("====   현 ${CURRENT_MP_ERACO}대 국회의원 ${jsonMPs.count()}명 전체 동기화 처리 시간 => ${totalTime / 1000}초   ====")
            }
        } catch (e: Exception) {
            logger.error("현 국회의원 동기화 배치작업 처리 중 오류 발생: ${e.message}", e)
        }
    }

    suspend fun testDelete() {
        allLanguages.forEach {
            val mpsTable = getMPsTable(it)
            mpsTable.deleteAll()
        }
    }
}