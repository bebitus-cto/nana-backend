package com.nana.nana.domain.feature.translation.openai

import com.nana.nana.domain.feature.assemblysync.lp.batchprocessor.MPNameBatchProcessor
import com.nana.nana.domain.feature.assemblysync.mp.datamodel.MPSyncDataModel
import com.nana.nana.domain.feature.log.LogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class GPTSyncRetryService(
    private val applicationScope: CoroutineScope,
    private val mpNameBatchProcessor: MPNameBatchProcessor,
    private val gptRetryRepository: GPTRetryRepository,
    private val logRepository: LogRepository
) {

    private val logger = LoggerFactory.getLogger(GPTSyncRetryService::class.java)

    suspend fun retryFailedMPBatch() {
        logger.warn("[국회의원 GPT 번역 오류 재시도]")
        newSuspendedTransaction {
            val failedLogs: Map<Int, Set<String>> = logRepository.getFailedMPNameTransIds()

            if (failedLogs.isEmpty()) {
                return@newSuspendedTransaction
            }

            val allFailedMPIds: Set<String> = failedLogs.values.flatten().toSet()

            val failedMPs: List<MPSyncDataModel> =
                gptRetryRepository.selectMPsWithMissingMultilingualName(allFailedMPIds)

            logger.warn("failedMPs: $failedMPs")

            if (failedMPs.isEmpty()) {
                logger.info("재시도할 MP가 없습니다. 모든 MP에 대해 영어 번역이 이미 존재합니다.")
                logRepository.markFailedMpNamesAsRetried(failedLogs.keys.toList(), true)
                return@newSuspendedTransaction
            }

            val isResolved = mpNameBatchProcessor.retryAddMPs(failedMPs)
            logRepository.markFailedMpNamesAsRetried(failedLogs.keys.toList(), isResolved)
        }
    }

    @Scheduled(cron = "0 0 * * * ?") // 매 시간 정각에 실행
    suspend fun retryFailedLPBatch() {

    }
}