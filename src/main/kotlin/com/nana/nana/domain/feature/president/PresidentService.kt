package com.nana.nana.domain.feature.president

import com.nana.nana.domain.feature.assemblysync.pr.SyncTransPrService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PresidentService(
    private val repository: PresidentRepository,
    private val translationService: SyncTransPrService
) {

    private val logger: Logger = LoggerFactory.getLogger(PresidentService::class.java)

}
