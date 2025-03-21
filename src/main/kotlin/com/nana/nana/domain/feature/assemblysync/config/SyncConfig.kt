package com.nana.nana.domain.feature.assemblysync.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.config.ScheduledTaskRegistrar

object SyncConfig {

    const val START_MP_ERACO = 1
    const val CURRENT_MP_ERACO = 22
    const val CURRENT_PRESIDENT_ERACO = 20
    const val LAW_PROPOSAL_PAGE_SIZE = 1000

    @Configuration
    @EnableScheduling
    class SchedulerConfig : SchedulingConfigurer {

        @Bean(name = ["mpTaskScheduler"])
        fun mpTaskScheduler(): ThreadPoolTaskScheduler {
            val scheduler = ThreadPoolTaskScheduler()
            scheduler.poolSize = 10  // 국회의원 동기화 전용 스레드 풀 크기
            scheduler.setThreadNamePrefix("MP-Sync-")
            scheduler.initialize()
            return scheduler
        }

        @Bean(name = ["lpTaskScheduler"])
        fun lpTaskScheduler(): ThreadPoolTaskScheduler {
            val scheduler = ThreadPoolTaskScheduler()
            scheduler.poolSize = 10  // 법안 동기화 전용 스레드 풀 크기
            scheduler.setThreadNamePrefix("LP-Sync-")
            scheduler.initialize()
            return scheduler
        }

        override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
            taskRegistrar.setScheduler(mpTaskScheduler())
            taskRegistrar.setScheduler(lpTaskScheduler())
        }
    }
}

