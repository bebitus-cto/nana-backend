package com.nana.nana.domain.feature.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import javax.annotation.PreDestroy

@Configuration
class CoroutineConfiguration {

    @Bean
    fun applicationScope(): CoroutineScope {
        return CoroutineScope(Job() + Dispatchers.Default)
    }
}

@Component
class CoroutineLifecycleManager(
    private val applicationScope: CoroutineScope
) {
    @PreDestroy
    fun onShutdown() {
        applicationScope.cancel("어플리케이션 종료 - 코루틴 종료")
    }
}