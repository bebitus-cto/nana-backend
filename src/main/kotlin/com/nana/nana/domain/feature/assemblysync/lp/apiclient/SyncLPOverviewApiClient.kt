package com.nana.nana.domain.feature.assemblysync.lp.apiclient

import com.nana.nana.config.WebClientConfig.Companion.ASSEMBLY_QUALIFIER
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.Url.LAW_PROPOSAL_OVERVIEW_URL
import com.nana.nana.config.ApiConfig.defaultJson
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.getOpenAssemblyBaseUriBuilder
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.toDecodedUriString
import com.nana.nana.domain.feature.assemblysync.apihelper.handleOpenAssemblyApiResponse
import com.nana.nana.domain.feature.assemblysync.config.SyncConfig.CURRENT_MP_ERACO
import com.nana.nana.domain.feature.assemblysync.config.SyncConfig.LAW_PROPOSAL_PAGE_SIZE
import com.nana.nana.domain.feature.assemblysync.lp.response.LPOverviewApiResponse
import com.nana.nana.domain.feature.log.LogLPSyncRepository
import com.nana.nana.util.WebParser
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
class SyncLPOverviewApiClient(
    @Qualifier(ASSEMBLY_QUALIFIER) private val webClient: WebClient,
) {

    private val logger: Logger = LoggerFactory.getLogger(SyncLPOverviewApiClient::class.java)

    suspend fun generateLPIdSet(): Set<String> = coroutineScope {
        logger.info("=== 최신 의안 번호 조회 시작 ===")

        try {
            val url = getOpenAssemblyBaseUriBuilder(LAW_PROPOSAL_OVERVIEW_URL)
                .queryParam("AGE", CURRENT_MP_ERACO)
                .queryParam("pSize", 1) // 최신 한 건만 가져오기
                .queryParam("pIndex", 1)
                .toDecodedUriString()

            val latestBillId = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String::class.java)
                .awaitSingleOrNull()
                ?.let { body ->
                    defaultJson.decodeFromString<LPOverviewApiResponse>(body)
                        .handleOpenAssemblyApiResponse(
                            onSuccess = { _, pagedItems -> pagedItems?.firstOrNull()?.id },
                            onError = { e ->
                                logger.error("의안 최신 ID 조회 실패: ${e.message}", e)
                                null
                            }
                        )
                }

            return@coroutineScope latestBillId?.toIntOrNull()?.takeIf { it >= 2200001 }?.let { latestNumber ->
                (2200001..latestNumber).map { it.toString() }.toSet().also {
                    logger.info("✅ 총 ${it.size}개의 의안 ID 생성됨 (2200001 ~ $latestNumber)")
                }
            } ?: run {
                logger.warn("❌ 유효한 최신 의안 ID를 찾을 수 없음")
                emptySet()
            }

        } catch (e: Exception) {
            logger.error("의안 ID 생성 중 오류 발생: ${e.message}", e)
            emptySet()
        }
    }
}