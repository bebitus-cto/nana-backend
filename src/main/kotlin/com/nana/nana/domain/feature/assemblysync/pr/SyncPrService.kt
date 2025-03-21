package com.nana.nana.domain.feature.assemblysync.pr

import com.nana.nana.domain.feature.assemblysync.pr.apiclient.AllPrsJsonDownloader.getAllPrsJson
import com.nana.nana.domain.feature.assemblysync.pr.datamodel.PrSyncDataModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

/**
 * json 파일 삽입으로 대체
 */
@Service
class SyncPrService(
    private val syncPrRepository: SyncPrRepository,
    private val transPrService: SyncTransPrService,
    private val applicationScope: CoroutineScope
) {

    private val logger: Logger = LoggerFactory.getLogger(SyncPrService::class.java)

    fun syncAllPrs() {
        logger.info("📌 [역대 및 현 대통령] 동기화 시작")

        val jsonPrs = getAllPrsJson()
        val prDataModels = jsonPrs.map { PrSyncDataModel.toDataModel(it) }

        logger.info("✅ [역대 및 현 대통령] ${jsonPrs.count()}명 상세정보 불러오기 완료")
        try {
            applicationScope.launch {
                measureTimeMillis {
                    newSuspendedTransaction {

                        // json 파일 삽입으로 대체
//                        syncPrRepository.batchUpsertTransPrsDetailMultiLang(prDataModels)
//                        syncPrRepository.batchUpsertPrsDetail(prDataModels)
//                        transPrService.batchUpsertPrsDetail()
                    }
                }.let { totalTime ->
                    logger.info("====   역대 및 현 대통령 ${jsonPrs.count()}명 전체 동기화 처리 시간 => ${totalTime / 1000}초   ====")
                }
            }
        } catch (e: Exception) {
            logger.error("대통령 데이터 처리 중 오류 발생: ${e.message}", e)
        }
    }
}
