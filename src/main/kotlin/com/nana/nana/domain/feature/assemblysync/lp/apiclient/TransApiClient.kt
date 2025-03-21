package com.nana.nana.domain.feature.assemblysync.lp.apiclient

import com.nana.nana.config.ApiConfig.defaultJson
import com.nana.nana.config.WebClientConfig.Companion.BASE_TRANS_URL
import com.nana.nana.domain.feature.translation.google.LibreTransRequest
import com.nana.nana.domain.feature.translation.google.LibreTransResponse
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import kotlinx.serialization.encodeToString
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.http.HttpClient
import java.nio.charset.StandardCharsets

@Component
class TransApiClient {

    private val logger = LoggerFactory.getLogger(TransApiClient::class.java)

    val httpClient = SslContextBuilder.forClient()
        .trustManager(InsecureTrustManagerFactory.INSTANCE)
        .build()
        .let { sslContext ->
            reactor.netty.http.client.HttpClient.create().secure { spec ->
                spec.sslContext(sslContext)
            }
        }

    val webClient: WebClient = WebClient.builder()
        .baseUrl(BASE_TRANS_URL)
//        .clientConnector(ReactorClientHttpConnector(httpClient))
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .filter { request, next ->
            println("Request: ${request.method()} ${request.url()}")
            println("Headers: ${request.headers()}")
            next.exchange(request)
        }
        .build()

    suspend fun requestTranslation(
        id: String,
        texts: List<String>,
        sourceLanguage: String,
        targetLanguage: String
    ): List<String> {
        logger.info("번역 요청 (id=$id) | $sourceLanguage -> $targetLanguage | texts=$texts")

        if (texts.isEmpty()) {
            logger.info("번역할 텍스트가 없으므로 빈 리스트 반환")
            return emptyList()
        }

        val joinedText = texts.map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(separator = ",")
            .also { logger.debug("번역요청텍스트: $it") }

        val transRequest = LibreTransRequest(
            q = "뭐가문제야도대체?",
            source = sourceLanguage,
            target = targetLanguage
        )

        val requestBody = "{\"q\": \"안녕하세요, 세계!\", \"source\": \"ko\", \"target\": \"en\", \"format\": \"text\"}"
//        val requestBody = defaultJson.encodeToString(transRequest)
        logger.debug("인코딩된 요청 JSON: $requestBody")

        val response = webClient.post()
            .uri("/translate")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(LibreTransResponse::class.java)
            .doOnNext { response -> logger.info("Response: $response") }
            .doOnError { error -> logger.error("Error: ", error) }
            .block() ?: throw RuntimeException("응답이 비어 있습니다")

        logger.info("번역 결과 (id=$id): ${response.translatedTexts}")

        // 응답으로 받은 단일 문자열을 콤마 기준으로 분리하고, 트림하여 리스트로 반환합니다.
        return response.translatedTexts.split(",")
            .map { it.trim() }
    }
}