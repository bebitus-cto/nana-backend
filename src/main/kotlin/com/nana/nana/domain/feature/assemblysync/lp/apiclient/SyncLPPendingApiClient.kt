package com.nana.nana.domain.feature.assemblysync.lp.apiclient

import com.nana.nana.config.WebClientConfig.Companion.ASSEMBLY_QUALIFIER
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.Url.PENDING_LAW_PROPOSAL_URL
import com.nana.nana.config.ApiConfig.defaultJson
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.getOpenAssemblyBaseUriBuilder
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.toDecodedUriString
import com.nana.nana.domain.feature.assemblysync.apihelper.handleOpenAssemblyApiResponse
import com.nana.nana.domain.feature.assemblysync.config.SyncConfig.CURRENT_MP_ERACO
import com.nana.nana.domain.feature.assemblysync.config.SyncConfig.LAW_PROPOSAL_PAGE_SIZE
import com.nana.nana.domain.feature.assemblysync.lp.response.PendingLPApiResponse
import com.nana.nana.domain.feature.assemblysync.lp.response.PendingLPResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class SyncLPPendingApiClient(
    @Qualifier(ASSEMBLY_QUALIFIER) private val webClient: WebClient
) {

    private val logger: Logger = LoggerFactory.getLogger(SyncLPPendingApiClient::class.java)

    suspend fun fetchPendings(): Map<String, PendingLPResponse> = coroutineScope {
        logger.info("=== $CURRENT_MP_ERACO 대 계류 의안 불러오기 시작 ===")
        val semaphore = Semaphore(3)
        val resultMap = mutableMapOf<String, PendingLPResponse>()
        var totalPages = 1

        try {
            // 1️⃣ 첫 번째 요청: 전체 페이지 수 계산 및 첫 페이지 데이터 수집
            val firstPageUrl = getOpenAssemblyBaseUriBuilder(PENDING_LAW_PROPOSAL_URL)
                .queryParam("AGE", CURRENT_MP_ERACO)
                .queryParam("pSize", LAW_PROPOSAL_PAGE_SIZE)
                .queryParam("pIndex", 1)
                .toDecodedUriString()

            val firstPageBody: String = webClient.get()
                .uri(firstPageUrl)
                .retrieve()
                .bodyToMono(String::class.java)
                .awaitSingleOrNull()
                .orEmpty()

            val firstPageData = defaultJson.decodeFromString<PendingLPApiResponse>(firstPageBody)

            val firstPageResults = firstPageData.handleOpenAssemblyApiResponse(
                onSuccess = { itemTotalCount, pagedItems ->
                    totalPages = if (itemTotalCount % LAW_PROPOSAL_PAGE_SIZE == 0) {
                        itemTotalCount / LAW_PROPOSAL_PAGE_SIZE
                    } else {
                        (itemTotalCount / LAW_PROPOSAL_PAGE_SIZE) + 1
                    }.let { maxOf(1, it) }

                    // 첫 페이지 항목들을 id 기준 맵으로 변환하여 추가
                    pagedItems?.associateBy { it.id } ?: emptyMap()
                },
                onError = { e ->
                    logger.error("계류 의안 첫 페이지 조회 실패: ${e.message}", e)
                    emptyMap()
                }
            )

            resultMap.putAll(firstPageResults)

            // 2️⃣ 2페이지부터 병렬 요청 실행 (전체 페이지가 1페이지보다 많다면)
            if (totalPages > 1) {
                val deferredResults = (2..totalPages).map { pageIndex ->
                    async(Dispatchers.IO) {
                        semaphore.withPermit {
                            fetchPendingMapForPage(pageIndex)
                        }
                    }
                }
                // 각 페이지별 맵을 받아와서 합침
                val additionalMaps = deferredResults.awaitAll()
                additionalMaps.forEach { map ->
                    resultMap.putAll(map)
                }
            }
        } catch (e: Exception) {
            logger.error("계류 의안 불러오기 실패: ${e.message}", e)
        }

        logger.info("✅ $CURRENT_MP_ERACO 대 계류 의안 불러오기 완료 (${resultMap.size}건)")
        resultMap
    }

    /**
     * 특정 페이지의 계류 의안 항목을 id를 키로 하는 맵으로 반환하는 함수
     */
    private suspend fun fetchPendingMapForPage(pageIndex: Int): Map<String, PendingLPResponse> = try {
        val url = getOpenAssemblyBaseUriBuilder(PENDING_LAW_PROPOSAL_URL)
            .queryParam("AGE", CURRENT_MP_ERACO)
            .queryParam("pSize", LAW_PROPOSAL_PAGE_SIZE)
            .queryParam("pIndex", pageIndex)
            .toDecodedUriString()

        val body: String = webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(String::class.java)
            .awaitSingleOrNull()
            .orEmpty()

        val response = defaultJson.decodeFromString<PendingLPApiResponse>(body)
        response.handleOpenAssemblyApiResponse(
            onSuccess = { _, pagedItems ->
                pagedItems?.associateBy { it.id } ?: emptyMap()
            },
            onError = { e ->
                logger.error("계류 의안 페이지 ($pageIndex) 조회 실패: ${e.message}", e)
                emptyMap()
            }
        )
    } catch (e: Exception) {
        logger.error("계류 의안 페이지 ($pageIndex) 조회 실패: ${e.message}", e)
        emptyMap()
    }
}