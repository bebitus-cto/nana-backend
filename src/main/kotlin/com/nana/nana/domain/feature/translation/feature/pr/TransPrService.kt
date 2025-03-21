package com.nana.nana.domain.feature.translation.feature.pr

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TransPrService(
    private val transPrRepository: TransPrRepository
) {

    private val logger: Logger = LoggerFactory.getLogger(TransPrService::class.java)

    fun batchUpsertPrsDetail() {
        transPrRepository.batchUpsertPrsDetail()
    }
}