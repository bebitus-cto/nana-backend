package com.nana.nana.domain.feature.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

@Configuration
class GCPStorageConfig {

    @Bean
    fun provideStorage(): Storage {
        return try {
            val resource = ClassPathResource("key/nana-4151d-cb194f126723.json")
            StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(resource.inputStream))
                .build()
                .service
        } catch (e: Exception) {
            throw RuntimeException("GCP Storage 초기화 실패: ${e.message}", e)
        }
    }
}