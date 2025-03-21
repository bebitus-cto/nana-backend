package com.nana.nana.domain.feature.assemblysync.lp.batchprocessor

import com.nana.nana.domain.feature.assemblysync.lp.SyncLPUrlRepository
import com.nana.nana.domain.feature.assemblysync.lp.apiclient.SyncLPFileApiClient
import com.nana.nana.domain.feature.assemblysync.lp.datamodel.LPUrlDataModel
import com.nana.nana.domain.feature.log.LogLPSyncRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.math.pow

@Component
class LPFileBatchProcessor(
    private val syncLPUrlRepository: SyncLPUrlRepository,
    private val syncLPFileApiClient: SyncLPFileApiClient,
    private val logLPSyncRepository: LogLPSyncRepository
) {

    private val semaphore = Semaphore(3)
    private val mutex = Mutex()
    private val logger: Logger = LoggerFactory.getLogger(LPFileBatchProcessor::class.java)
    private val pendingQueue = mutableListOf<LPFileUploadRequest>()

    suspend fun add(
        newLPRawUrls: List<LPUrlDataModel>,
        existingLPUrls: Map<String, LPUrlDataModel?>
    ) {
        val urlsToUpload = mutableListOf<LPFileUploadRequest>()

        newLPRawUrls.forEach { rawUrl ->
            val lpId = rawUrl.lpId
            if (lpId != null) {
                val existingUrl = existingLPUrls[lpId]
                val hwpUrl = rawUrl.rawHWPUrl.orEmpty()
                if (hwpUrl.isNotEmpty() && hwpUrl != existingUrl?.rawHWPUrl) {
                    urlsToUpload.add(LPFileUploadRequest(lpId = lpId, fileType = "HWP", fileUrl = hwpUrl))
                }
                val pdfUrl = rawUrl.rawPDFUrl.orEmpty()
                if (pdfUrl.isNotEmpty() && pdfUrl != existingUrl?.rawPDFUrl) {
                    urlsToUpload.add(LPFileUploadRequest(lpId = lpId, fileType = "PDF", fileUrl = pdfUrl))
                }
            }
        }

        logger.info("의안 링크 업데이트: ${urlsToUpload.count()}")

        if (urlsToUpload.isNotEmpty()) {
            val queueSnapshot = mutex.withLock {
                pendingQueue.addAll(urlsToUpload)
                val snapshot = pendingQueue.toList()
                pendingQueue.clear()
                snapshot
            }
            processBatch(queueSnapshot)
        }
    }

    private suspend fun processBatch(queue: List<LPFileUploadRequest>) = coroutineScope {
        logger.info("🚀 파일 업로드 시작: ${queue.count()}건 처리")

        val deferredUploads = queue.chunked(3)
            .flatMap { chunk ->
                chunk.map { request ->
                    async(Dispatchers.IO) {
                        semaphore.withPermit {
                            var attempt = 0
                            var uploadedUrl: String? = null

                            while (attempt < 3 && uploadedUrl == null) {
                                try {
                                    uploadedUrl = syncLPFileApiClient.downloadAndUploadLPFiles(
                                        id = request.lpId,
                                        fileUrl = request.fileUrl,
                                        fileType = request.fileType
                                    )
                                } catch (e: Exception) {
                                    if (e.toString().contains("429")) {
                                        logLPSyncRepository.logLPSyncError(
                                            "429 에러 발생: ${request.lpId}(${request.fileType}), 재시도 시도: ${attempt + 1}",
                                            request.lpId
                                        )
                                        val delayTime = 3000L * (2.0.pow(attempt.toDouble())).toLong()
                                        delay(delayTime)
                                        attempt++
                                    } else {
                                        logLPSyncRepository.logLPSyncError(
                                            "에러 발생: ${request.lpId}(${request.fileType}): ${e.message}",
                                            request.lpId
                                        )
                                        break
                                    }
                                }
                            }
                            if (uploadedUrl != null) {
                                LPFileUploadResponse(
                                    lpId = request.lpId,
                                    fileType = request.fileType,
                                    uploadedUrl = uploadedUrl
                                )
                            } else {
                                logLPSyncRepository.logLPSyncError(
                                    "의안 원문 업로드 실패: ${request.lpId} (${request.fileType}) after $attempt attempts",
                                    request.lpId
                                )
                                null
                            }
                        }
                    }
                }
            }

        val uploadResults = deferredUploads.awaitAll().filterNotNull()

        val groupedResults = uploadResults
            .groupBy({ it.lpId }) { response ->
                response.fileType to response.uploadedUrl
            }
            .mapValues { (lpId, filePairs) ->
                val fileMap = filePairs.toMap()
                LPUrlDataModel(
                    lpId = lpId,
                    gcpHWPUrl = fileMap["HWP"],
                    gcpPDFUrl = fileMap["PDF"]
                )
            }
        if (groupedResults.isNotEmpty()) {
            syncLPUrlRepository.batchUpsertLPFileGCPUrls(groupedResults.keys, groupedResults)
            logger.info("✅ 원문 파일 업데이트 완료: ${groupedResults.size}건")
        }
    }
}

data class LPFileUploadRequest(
    val lpId: String,
    val fileType: String,
    val fileUrl: String
)

data class LPFileUploadResponse(
    val lpId: String,
    val fileType: String,
    val uploadedUrl: String
)