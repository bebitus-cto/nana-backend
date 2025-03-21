package com.nana.nana.domain.feature.assemblysync.lp.apiclient

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.nana.nana.domain.feature.log.LogLPSyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.io.File
import java.nio.file.Files

@Component
class SyncLPFileApiClient(
    private val storage: Storage,
    private val logLPSyncRepository: LogLPSyncRepository
) {

    private val logger: Logger = LoggerFactory.getLogger(SyncLPFileApiClient::class.java)
    private val gcpWebClient: WebClient = WebClient.create()

    suspend fun downloadAndUploadLPFiles(
        fileUrl: String,
        id: String,
        fileType: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val fileBytes: ByteArray = gcpWebClient.get()
                .uri(fileUrl)
                .retrieve()
                .bodyToMono(ByteArray::class.java)
                .awaitSingle()

            // 2. 임시 파일 생성 및 파일 기록
            val tempFile = File.createTempFile(id, ".$fileType")
            tempFile.writeBytes(fileBytes)

            if (!tempFile.exists() || tempFile.length() <= 0) {
                newSuspendedTransaction {
                    logLPSyncRepository.logLPSyncError("$fileType 다운로드 실패 또는 비어 있음: ${tempFile.absolutePath}", id)
                }
                return@withContext null
            }
            logger.info("$fileType 다운로드 성공: ${tempFile.absolutePath}, 크기: ${tempFile.length()} bytes")

            // 3. GCP Storage 업로드
            val blobId = BlobId.of(BUCKET_NAME, "$id.$fileType")
            val blobInfo = BlobInfo.newBuilder(blobId).build()
            storage.create(blobInfo, Files.readAllBytes(tempFile.toPath()))

            // 4. 업로드 성공 시, 공개 URL 구성 (버킷이 공개 상태여야 함)
            val gcpFileUrl = "https://storage.googleapis.com/$BUCKET_NAME/$id.$fileType"
            logger.info("GCP 업로드 성공: $gcpFileUrl")
            return@withContext gcpFileUrl

        } catch (e: Exception) {
            newSuspendedTransaction {
                logLPSyncRepository.logLPSyncError("$fileType 파일 다운로드 및 업로드 실패: $e", id)
            }
            return@withContext null
        }
    }

    companion object {
        private const val BUCKET_NAME = "nana-lawproposal"
    }
}