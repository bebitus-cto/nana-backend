package com.nana.nana.domain.feature.assemblysync.lp.apiclient

import com.nana.nana.config.WebClientConfig.Companion.ASSEMBLY_QUALIFIER
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.Url.ENDED_LEGISLATION_URL
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.Url.PROGRESS_LEGISLATION_URL
import com.nana.nana.config.ApiConfig.defaultJson
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.getOpenAssemblyBaseUriBuilder
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.toDecodedUriString
import com.nana.nana.domain.feature.assemblysync.apihelper.handleOpenAssemblyApiResponse
import com.nana.nana.domain.feature.assemblysync.config.SyncConfig.CURRENT_MP_ERACO
import com.nana.nana.domain.feature.assemblysync.config.SyncConfig.LAW_PROPOSAL_PAGE_SIZE
import com.nana.nana.domain.feature.assemblysync.lp.response.EndedLegisApiResponse
import com.nana.nana.domain.feature.assemblysync.lp.response.EndedLegisResponse
import com.nana.nana.domain.feature.assemblysync.lp.response.ProgressLegisApiResponse
import com.nana.nana.domain.feature.assemblysync.lp.response.ProgressLegisResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class SyncLPLegisApiClient(
    @Qualifier(ASSEMBLY_QUALIFIER) private val webClient: WebClient
) {

    private val logger: Logger = LoggerFactory.getLogger(SyncLPLegisApiClient::class.java)

    suspend fun fetchProgressLegis(): Map<String, ProgressLegisResponse> = coroutineScope {
        logger.info("=== $CURRENT_MP_ERACO 대 진행중 입법예고 불러오기 시작 ===")
        val semaphore = Semaphore(3)
        val resultMap = mutableMapOf<String, ProgressLegisResponse>()
        var totalPages = 0

        try {
            // ✅ 1. 첫 번째 요청 (전체 페이지 수 계산 및 첫 페이지 데이터 수집)
            val firstPageUrl = getOpenAssemblyBaseUriBuilder(PROGRESS_LEGISLATION_URL)
                .queryParam("pSize", LAW_PROPOSAL_PAGE_SIZE)
                .queryParam("pIndex", 1)
                .toDecodedUriString()

            val firstBody = webClient.get()
                .uri(firstPageUrl)
                .retrieve()
                .bodyToMono(String::class.java)
                .awaitSingleOrNull()
                .orEmpty()

            val firstResponse = defaultJson.decodeFromString<ProgressLegisApiResponse>(firstBody)

            // 첫 페이지 데이터를 Map 형태로 변환
            val firstPageMap = firstResponse.handleOpenAssemblyApiResponse(
                onSuccess = { itemTotalCount, pagedItems ->
                    totalPages = (itemTotalCount + LAW_PROPOSAL_PAGE_SIZE - 1) / LAW_PROPOSAL_PAGE_SIZE
                    logger.info("✅ 전체 페이지 수: $totalPages")
                    pagedItems?.associateBy { it.id } ?: emptyMap()
                },
                onError = { e ->
                    logger.error("진행중 입법예고 오류 (페이지 1): ${e.message}", e)
                    emptyMap()
                }
            )
            resultMap.putAll(firstPageMap)

            // ✅ 2. 2페이지부터 병렬 요청 실행 (있다면)
            if (totalPages > 1) {
                val deferredResults = (2..totalPages).map { pageIndex ->
                    async(Dispatchers.IO) {
                        semaphore.withPermit {
                            fetchProgressLegisPage(pageIndex)
                        }
                    }
                }
                val additionalMaps = deferredResults.awaitAll() // 각 페이지별 Map 반환
                additionalMaps.forEach { map ->
                    resultMap.putAll(map)
                }
            }
        } catch (e: Exception) {
            logger.error("진행중 입법예고 오류: ${e.message}", e)
        }

        logger.info("=== $CURRENT_MP_ERACO 대 진행중 입법예고 불러오기 끝 (총 ${resultMap.count()}건) ===")
        resultMap
    }

    private suspend fun fetchProgressLegisPage(pageIndex: Int): Map<String, ProgressLegisResponse> = try {
        val url = getOpenAssemblyBaseUriBuilder(PROGRESS_LEGISLATION_URL)
            .queryParam("pSize", LAW_PROPOSAL_PAGE_SIZE)
            .queryParam("pIndex", pageIndex)
            .toDecodedUriString()

        val body = webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(String::class.java)
            .awaitSingleOrNull()
            .orEmpty()

        val response = defaultJson.decodeFromString<ProgressLegisApiResponse>(body)

        response.handleOpenAssemblyApiResponse(
            onSuccess = { _, pagedItems ->
                pagedItems?.associateBy { it.id } ?: emptyMap()
            },
            onError = { e ->
                logger.error("진행중 입법예고 오류 (페이지 $pageIndex): ${e.message}", e)
                emptyMap()
            }
        )
    } catch (e: Exception) {
        logger.error("진행중 입법예고 요청 실패 (페이지 $pageIndex): ${e.message}", e)
        emptyMap()
    }

    suspend fun fetchEndedLegis(): Map<String, EndedLegisResponse> = coroutineScope {
        logger.info("=== $CURRENT_MP_ERACO 대 종료된 입법예고 불러오기 시작 ===")
        val semaphore = Semaphore(3)
        val resultMap = mutableMapOf<String, EndedLegisResponse>()
        var totalPages = 0

        try {
            // ✅ 1. 첫 번째 요청 (전체 페이지 수 계산 및 첫 페이지 데이터 수집)
            val firstPageUrl = getOpenAssemblyBaseUriBuilder(ENDED_LEGISLATION_URL)
                .queryParam("AGE", CURRENT_MP_ERACO)
                .queryParam("pSize", LAW_PROPOSAL_PAGE_SIZE)
                .queryParam("pIndex", 1)
                .toDecodedUriString()

            val firstBody = webClient.get()
                .uri(firstPageUrl)
                .retrieve()
                .bodyToMono(String::class.java)
                .awaitSingleOrNull()
                .orEmpty()

            val firstResponse = defaultJson.decodeFromString<EndedLegisApiResponse>(firstBody)

            // 첫 페이지 데이터를 Map 형태로 변환
            val firstPageMap = firstResponse.handleOpenAssemblyApiResponse(
                onSuccess = { itemTotalCount, pagedItems ->
                    totalPages = (itemTotalCount + LAW_PROPOSAL_PAGE_SIZE - 1) / LAW_PROPOSAL_PAGE_SIZE
                    logger.info("✅ 전체 페이지 수: $totalPages")
                    pagedItems?.associateBy { it.id } ?: emptyMap()
                },
                onError = { e ->
                    logger.error("종료된 입법예고 오류 (페이지 1): ${e.message}", e)
                    emptyMap()
                }
            )
            resultMap.putAll(firstPageMap)

            // ✅ 2. 2페이지부터 병렬로 요청 실행 (있다면)
            if (totalPages > 1) {
                val deferredResults = (2..totalPages).map { pageIndex ->
                    async(Dispatchers.IO) {
                        semaphore.withPermit {
                            fetchEndedLegisPage(pageIndex)
                        }
                    }
                }
                val additionalMaps = deferredResults.awaitAll()
                additionalMaps.forEach { map ->
                    resultMap.putAll(map)
                }
            }
        } catch (e: Exception) {
            logger.error("종료된 입법예고 오류: ${e.message}", e)
        }

        logger.info("=== $CURRENT_MP_ERACO 대 종료된 입법예고 불러오기 끝 (총 ${resultMap.count()}건) ===")
        resultMap
    }

    private suspend fun fetchEndedLegisPage(pageIndex: Int): Map<String, EndedLegisResponse> = try {
        val url = getOpenAssemblyBaseUriBuilder(ENDED_LEGISLATION_URL)
            .queryParam("AGE", CURRENT_MP_ERACO)
            .queryParam("pSize", LAW_PROPOSAL_PAGE_SIZE)
            .queryParam("pIndex", pageIndex)
            .toDecodedUriString()

        val body = webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(String::class.java)
            .awaitSingleOrNull()
            .orEmpty()

        val response = defaultJson.decodeFromString<EndedLegisApiResponse>(body)

        response.handleOpenAssemblyApiResponse(
            onSuccess = { _, pagedItems ->
                pagedItems?.associateBy { it.id } ?: emptyMap()
            },
            onError = { e ->
                logger.error("종료된 입법예고 오류 (페이지 $pageIndex): ${e.message}", e)
                emptyMap()
            }
        )
    } catch (e: Exception) {
        logger.error("종료된 입법예고 요청 실패 (페이지 $pageIndex): ${e.message}", e)
        emptyMap()
    }
}