package com.nana.nana.domain.feature.translation.openai.apiclient

import com.nana.nana.config.WebClientConfig.Companion.GPT_QUALIFIER
import com.nana.nana.config.ApiConfig.defaultJson
import com.nana.nana.domain.feature.assemblysync.lp.datamodel.LPSyncDataModel
import com.nana.nana.domain.feature.assemblysync.mp.datamodel.MPSyncDataModel
import com.nana.nana.domain.feature.log.LogRepository
import com.nana.nana.domain.feature.translation.openai.GPTRequest
import com.nana.nana.domain.feature.translation.openai.GPTResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.serialization.encodeToString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import kotlin.math.pow

@Service
class GPTGenApiClient(
    @Qualifier(GPT_QUALIFIER) private val gptWebClient: WebClient,
    private val logRepository: LogRepository
) {
    private val logger: Logger = LoggerFactory.getLogger(GPTGenApiClient::class.java)

    suspend fun batchTranslateLPNicknamesMultiLang(
        batch: List<LPSyncDataModel>,
        gptRequest: GPTRequest,
        allLanguages: List<String>
    ): Map<String, Map<String, String>> {

        val logger: Logger = LoggerFactory.getLogger("GPTGenApiClient")
        val result = mutableMapOf<String, MutableMap<String, String>>()
        batch.forEach { lp -> result[lp.id] = mutableMapOf() }

        var attempt = 0
        var mappingForAll: Map<String, Map<String, String>>? = null

        while (attempt < 3 && mappingForAll == null) {
            try {
                logger.info("🚀 GPT 요청 시작 (시도 $attempt) - 요청 배치 크기: ${batch.size}")
                logger.info("📨 GPT 요청 데이터: ${defaultJson.encodeToString(gptRequest)}") // 요청 JSON 출력

                val requestBody = defaultJson.encodeToString(gptRequest)

                val responseBody = gptWebClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String::class.java)
                    .awaitSingleOrNull() ?: run {
                    logger.error("❌ GPT API 응답이 비어 있음! (시도 $attempt)")
                    logGptLPBatchTransError(batch, gptRequest, allLanguages, "GPT API 응답이 비어 있음", null, attempt)
                    return emptyMap()
                }

                logger.info("✅ GPT 원본 응답 (시도 $attempt): \n$responseBody") // 원본 응답 로깅

                val gptResponse = defaultJson.decodeFromString<GPTResponse>(responseBody)
                val rawResponse = gptResponse.choices.firstOrNull()?.message?.content.orEmpty()
                logger.info("\n✅ GPT API 응답 내용 (시도 $attempt): \n$rawResponse")

                val lines = rawResponse.lines().filter { it.isNotBlank() }
                logger.info("🔎 GPT 응답 파싱 완료 - 예상 ${batch.size}개 / 실제 ${lines.count()}개")

                if (lines.count() != batch.count()) {
                    logger.error("❌ GPT 응답 개수 불일치! 예상 ${batch.count()}개, 실제 ${lines.count()}개")
                    logGptLPBatchTransError(batch, gptRequest, allLanguages, "GPT 응답 개수 불일치", rawResponse, attempt)
                    return emptyMap()
                }

                val mappingResult = mutableMapOf<String, Map<String, String>>()
                batch.forEachIndexed { index, lp ->
                    val line = lines.getOrNull(index) ?: run {
                        logger.warn("⚠️ GPT 번역 응답 파싱 실패 - 응답 누락 (Index: $index)")
                        return emptyMap()
                    }

                    val parts = line.split(" - ").map { it.trim() }
                    if (parts.size < 2 || parts[1].isEmpty()) {
                        logger.warn("⚠️ GPT 번역 응답 파싱 실패: '$line' (Index: $index)")
                        return emptyMap()
                    }

                    val rawNames = parts[1].split(",").map { it.trim() }
                    val names = rawNames.map { it.removePrefix("[").removeSuffix("]") }
                    val langMap = allLanguages.zip(names).toMap()
                    mappingResult[lp.id] = langMap
                }

                mappingForAll = mappingResult

            } catch (e: Exception) {
                attempt++

                logger.error("❌ GPT API 호출 실패 (시도 $attempt) - ${e.message}", e)

                if (attempt >= 3) {
                    logGptLPBatchTransError(batch, gptRequest, allLanguages, "GPT API 호출 완전히 실패", null, attempt)
                } else {
                    val delayTime = 2000L * (2.0.pow(attempt.toDouble())).toLong()
                    logger.warn("⚠️ GPT API 호출 실패 (시도 $attempt), ${delayTime}ms 후 재시도")
                    delay(delayTime)
                }
            }
        }

        if (mappingForAll.isNullOrEmpty()) {
            logger.error("❌ GPT 번역 결과가 비어 있음! API 응답 확인 필요")
            throw RuntimeException("GPT 번역 결과가 비어 있음! API 응답 확인 필요")
        }

        logger.info("✅ 최종 GPT 번역 결과 반환 - 데이터 크기: ${mappingForAll.size}")
        return mappingForAll
    }

    suspend fun batchTranslateMPnamesMultiLangExcludingKo(
        batch: List<MPSyncDataModel>,
        gptRequest: GPTRequest,
        modifiedTargetLanguages: List<String>
    ): Map<String, Map<String, String>> {

        val result = mutableMapOf<String, MutableMap<String, String?>>()

        batch.forEach { mp ->
            result[mp.id] = mutableMapOf()
        }

        var attempt = 0
        var mappingForAll: Map<String, Map<String, String>>? = null

        while (attempt < 3 && mappingForAll == null) {
            try {

                val requestBody = defaultJson.encodeToString(gptRequest)

                val responseBody = gptWebClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String::class.java)
                    .awaitSingleOrNull() ?: run {
                    logGptMPBatchTransError(
                        batch,
                        gptRequest,
                        modifiedTargetLanguages,
                        "GPT API 응답이 비어 있음",
                        null,
                        attempt
                    )
                    return emptyMap()
                }

                val gptResponse = defaultJson.decodeFromString<GPTResponse>(responseBody)
                val rawResponse = gptResponse.choices.firstOrNull()?.message?.content.orEmpty()
                logger.info("\n✅ GPT API 응답 내용: \n$rawResponse")

                val lines = rawResponse.lines().filter { it.isNotBlank() }
                if (lines.count() != batch.count()) {
                    logGptMPBatchTransError(
                        batch,
                        gptRequest,
                        modifiedTargetLanguages,
                        "GPT 응답 개수 불일치: 예상 ${batch.count()}개, 실제 ${lines.count()}개",
                        rawResponse,
                        attempt
                    )
                    return emptyMap()
                }

                val mappingResult = mutableMapOf<String, Map<String, String>>()
                batch.forEachIndexed { index, (mpId, _) ->
                    val line = lines[index]
                    val parts = line.split(" - ").map { it.trim() }

                    if (parts.count() < 2 || parts[1].isEmpty()) {
                        logGptMPBatchTransError(
                            batch,
                            gptRequest,
                            modifiedTargetLanguages,
                            "GPT 번역 응답 파싱 실패: '$line'",
                            rawResponse,
                            attempt
                        )
                        return emptyMap()
                    }

                    val rawNames = parts[1].split(",").map { it.trim() }
                    val names = rawNames.map { it.removePrefix("[").removeSuffix("]") }
                    val langMap = modifiedTargetLanguages.zip(names).toMap()
                    mappingResult[mpId] = langMap
                }
                mappingForAll = mappingResult

            } catch (e: Exception) {
                attempt++

                if (attempt >= 3) {
                    logGptMPBatchTransError(
                        batch,
                        gptRequest,
                        modifiedTargetLanguages,
                        "GPT API 호출 완전히 실패",
                        null,
                        attempt
                    )
                } else {
                    val delayTime = 1000L * (2.0.pow(attempt.toDouble())).toLong()
                    logGptMPBatchTransError(
                        batch,
                        gptRequest,
                        modifiedTargetLanguages,
                        "GPT API 호출 실패 (시도 $attempt), ${delayTime}ms 후 재시도: ${e.message}",
                        null,
                        attempt
                    )
                    delay(delayTime)
                }
            }
        }
        return mappingForAll ?: emptyMap()
    }

    private suspend fun logGptMPBatchTransError(
        batch: List<MPSyncDataModel>,
        gptRequest: GPTRequest,
        modifiedTargetLanguages: List<String>,
        errorMessage: String,
        rawResponse: String? = null,
        attemptCount: Int
    ) {

        logRepository.logGptMPBatchTransError(
            batch,
            modifiedTargetLanguages,
            gptRequest,
            errorMessage,
            rawResponse,
            attemptCount
        )
    }

    private suspend fun logGptLPBatchTransError(
        batch: List<LPSyncDataModel>,
        gptRequest: GPTRequest,
        modifiedTargetLanguages: List<String>,
        errorMessage: String,
        rawResponse: String? = null,
        attemptCount: Int
    ) {

        logRepository.logGptLPBatchTranslationError(
            batch,
            modifiedTargetLanguages,
            gptRequest,
            errorMessage,
            rawResponse,
            attemptCount
        )
    }
}