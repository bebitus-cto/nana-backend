package com.nana.nana.domain.feature.assemblysync.pr

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SyncTransPrService(
    private val transPrRepository: SyncTransPrRepository
) {

    private val logger: Logger = LoggerFactory.getLogger(SyncTransPrService::class.java)

    fun batchUpsertPrsDetail() {
        transPrRepository.batchUpsertPrsDetail()
    }
}