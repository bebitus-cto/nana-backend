package com.nana.nana.domain.feature.assemblysync

import com.nana.nana.domain.feature.assemblysync.lp.SyncLPService
import com.nana.nana.domain.feature.assemblysync.mp.SyncMPService
import com.nana.nana.domain.feature.assemblysync.pr.SyncPrService
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class SyncStartupListener(
    private val syncPrService: SyncPrService,
    private val syncMPService: SyncMPService,
    private val syncLPService: SyncLPService,
) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(SyncStartupListener::class.java)

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
//        syncMPService.syncAllCurrentMPs()
        syncLPService.syncLPs()
//        syncLPService.syncLeftOffLPsLeadingParty()

//        syncPrService.syncAllPrs()
//        syncMPService.syncAllPastMPs()
    }
}
