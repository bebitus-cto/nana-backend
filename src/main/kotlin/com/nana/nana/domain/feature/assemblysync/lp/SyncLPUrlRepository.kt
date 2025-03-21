package com.nana.nana.domain.feature.assemblysync.lp

import com.nana.nana.domain.feature.assemblysync.lp.datamodel.LPUrlDataModel
import com.nana.nana.domain.feature.log.LogLPSyncRepository
import com.nana.nana.table.lp.LPUrlsTable
import com.nana.nana.table.lp.LPUrlsTable.linkUrl
import com.nana.nana.table.lp.LPUrlsTable.lpGCPHWPFileUrl
import com.nana.nana.table.lp.LPUrlsTable.lpGCPPDFFileUrl
import com.nana.nana.table.lp.LPUrlsTable.lpId
import com.nana.nana.table.lp.LPUrlsTable.lpRawHWPFileUrl
import com.nana.nana.table.lp.LPUrlsTable.lpRawPDFFileUrl
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class SyncLPUrlRepository(
    private val logLPSyncRepository: LogLPSyncRepository
) {

    private val logger: Logger = LoggerFactory.getLogger(SyncLPUrlRepository::class.java)

    /*없던 데이터라도 null로 저장*/
    suspend fun batchUpsertOnlyRawUrls(
        keys: Set<String>,
        dataModels: List<LPUrlDataModel>
    ): List<LPUrlDataModel> {

        try {
            val resultRows = LPUrlsTable.batchUpsert(dataModels) { url ->
                this[lpId] = url.lpId.orEmpty()
                this[linkUrl] = url.linkUrl
                this[lpRawHWPFileUrl] = url.rawHWPUrl
                this[lpRawPDFFileUrl] = url.rawPDFUrl
            }

            val inputCount = dataModels.count()
            val insertedCount = resultRows.count()

            val dbData = selectLPUrlsByIds(keys)
            val dbKeys = dbData.map { it.lpId }.toSet()
            val failedKeys = keys - dbKeys
            val failedData = dataModels.filter { url ->
                val key = url.lpId
                key in failedKeys
            }

            if (insertedCount == inputCount) {
                logger.warn("의안 Url 데이터 배치처리 성공: ${dbData.count()}개")
                return dbData
            } else {
                newSuspendedTransaction {
                    logLPSyncRepository.logLPSyncError(
                        "의안 Url 데이터 배치처리 중 Upsert 실패",
                        failedData.joinToString(separator = ",") { it.lpId.orEmpty() })
                }
                /*없던 데이터라도 null로 저장하므로 전체 집계*/
                return failedData
            }
        } catch (e: Exception) {
            newSuspendedTransaction {
                logLPSyncRepository.logLPSyncError(
                    "의안 Raw Url 배치처리 중 Upsert 실패 tryCatch", ""
                )
            }
            return emptyList()
        }
    }

    suspend fun batchUpsertLPFileGCPUrls(
        keys: Set<String>,
        uploadResults: Map<String, LPUrlDataModel>
    ) {
        if (uploadResults.isEmpty()) {
            logger.info("📌 업로드할 파일 URL 데이터가 없음")
            return
        }

        try {
            val resultRow = LPUrlsTable.batchUpsert(uploadResults.values) { urlDataModel ->
                this[lpId] = urlDataModel.lpId!!
                this[lpGCPHWPFileUrl] = urlDataModel.gcpHWPUrl
                this[lpGCPPDFFileUrl] = urlDataModel.gcpPDFUrl
            }

            val inputCount = uploadResults.count()
            val upsertedCount = resultRow.count()


            val dbData = selectLPUrlsByIds(keys)
            val dbKeys = dbData.map { it.lpId }.toSet()
            val failedKeys = keys - dbKeys
            val failedData = uploadResults.filter { (lpId, _) ->
                val key = lpId
                key in failedKeys
            }

            if (inputCount == upsertedCount) {
                logger.info("✅ GCP 파일 URL batch upsert 완료! (${uploadResults.count()}건)")
            } else {
                newSuspendedTransaction {
                    logLPSyncRepository.logLPSyncError(
                        "의안 GCP 배치처리 중 Upsert 실패",
                        failedData.keys.joinToString(separator = ",")
                    )
                }
            }
        } catch (e: Exception) {
            newSuspendedTransaction {
                logLPSyncRepository.logLPSyncError(
                    "의안 GCP 배치처리 중 Upsert 실패 tryCatch", ""
                )
            }
        }
    }

    private fun selectLPUrlsByIds(ids: Set<String>): List<LPUrlDataModel> {
        return LPUrlsTable.select { lpId inList ids }
            .map { row ->
                LPUrlDataModel(
                    lpId = row[lpId],
                    linkUrl = row[linkUrl],
                    rawHWPUrl = row[lpRawHWPFileUrl],
                    rawPDFUrl = row[lpRawPDFFileUrl],
                    gcpHWPUrl = row[lpGCPHWPFileUrl],
                    gcpPDFUrl = row[lpGCPPDFFileUrl]
                )
            }
            .toList()
    }
}