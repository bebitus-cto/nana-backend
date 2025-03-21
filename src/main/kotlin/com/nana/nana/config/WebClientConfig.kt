package com.nana.nana.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    private val logger: Logger = LoggerFactory.getLogger(WebClientConfig::class.java)

    // ✅ 국회 API
    @Bean
    @Qualifier(ASSEMBLY_QUALIFIER)
    fun assemblyWebClient(strategies: ExchangeStrategies): WebClient {

        return WebClient.builder()
            .baseUrl(OPEN_ASSEMBLY_BASE_URL)
            .exchangeStrategies(strategies)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }

    // ✅ 구글 로그인
    @Bean
    @Qualifier("googleOAuthWebClient")
    fun googleOAuthWebClient(strategies: ExchangeStrategies): WebClient {
        return WebClient.builder()
            .baseUrl("https://oauth2.googleapis.com")
            .exchangeStrategies(strategies)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }

    // ✅ GCP 버킷
    @Bean
    @Qualifier(GCP_UPLOAD_QUALIFIER)
    fun gcpUploadWebClient(strategies: ExchangeStrategies): WebClient {
        return WebClient.builder()
            .baseUrl(GCP_BASE_URL)
            .exchangeStrategies(strategies)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .build()
    }

    // ✅ GPT
    @Bean
    @Qualifier(GPT_QUALIFIER)
    fun gptWebClient(strategies: ExchangeStrategies): WebClient {
        return WebClient.builder()
            .baseUrl(GPT_BASE_URL)
            .exchangeStrategies(strategies)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("Authorization", "Bearer $OPEN_AI_API_KEY")
            .build()
    }


    @Bean
    fun webClientExchangeStrategies(): ExchangeStrategies = ExchangeStrategies.builder()
        .codecs { configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE) }
        .build()


    companion object {
        private const val MAX_IN_MEMORY_SIZE: Int = 5 * 1024 * 1024
        private const val OPEN_ASSEMBLY_BASE_URL = "https://open.assembly.go.kr/portal/openapi/"
        const val BASE_TRANS_URL = "http://localhost:5001"
        const val GCP_BASE_URL = "https://storage.googleapis.com"
        const val GPT_BASE_URL = "https://api.openai.com/v1/chat/completions"
        const val ASSEMBLY_QUALIFIER = "assemblyWebClient"
        const val GCP_UPLOAD_QUALIFIER = "gcpUploadWebClient"
        const val GPT_QUALIFIER = "gptWebClient"
    }
}