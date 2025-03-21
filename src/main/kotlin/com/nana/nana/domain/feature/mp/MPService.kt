package com.nana.nana.domain.feature.mp

import com.nana.nana.domain.feature.assemblysync.mp.SyncTransMPService
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MPService(
    private val mpRepository: MPRepository,
    private val syncTransMPService: SyncTransMPService,
    private val applciationScope: CoroutineScope,
) {

    private val logger: Logger = LoggerFactory.getLogger(MPService::class.java)


//    fun getMPsWithMissingNames() {
//        applciationScope.launch {
//            newSuspendedTransaction {
//                val missingMPDataModels = mpRepository.getMPsWithMissingNames()
//                logger.warn("GPT 번역 누락 리스트: ${missingMPDataModels.map { it.name }}")
//                mpNameAITransBatchProcessor.addMPs(missingMPDataModels, emptySet())
//            }
//        }
//    }
}