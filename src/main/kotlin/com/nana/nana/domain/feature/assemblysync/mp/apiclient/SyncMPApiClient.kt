package com.nana.nana.domain.feature.assemblysync.mp.apiclient

import com.nana.nana.config.WebClientConfig.Companion.ASSEMBLY_QUALIFIER
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.Url.MEMBERS_DETAIL_URL
import com.nana.nana.config.ApiConfig.defaultJson
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.getOpenAssemblyBaseUriBuilder
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.toDecodedUriString
import com.nana.nana.domain.feature.assemblysync.apihelper.handleOpenAssemblyApiResponse
import com.nana.nana.domain.feature.assemblysync.mp.datamodel.MPSyncDataModel
import com.nana.nana.domain.feature.assemblysync.mp.response.MPApiResponse
import com.nana.nana.domain.feature.assemblysync.mp.apiclient.json.SyncMPJsonProperty
import com.nana.nana.util.DateParser.extractYear
import com.nana.nana.util.DateParser.normalizeBirthDate
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient

@Component
class SyncMPApiClient(
    @Qualifier(ASSEMBLY_QUALIFIER) private val webClient: WebClient
) {

    private val logger: Logger = LoggerFactory.getLogger(SyncMPApiClient::class.java)


    suspend fun fetchMPDetail(property: SyncMPJsonProperty): MPSyncDataModel? {
        return try {
            val url = getOpenAssemblyBaseUriBuilder(MEMBERS_DETAIL_URL)
                .queryParam("NAAS_NM", property.name)
                .toDecodedUriString()

            val body = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String::class.java)
                .awaitSingleOrNull()
                .orEmpty()

            val response = defaultJson.decodeFromString<MPApiResponse>(body)

            response.handleOpenAssemblyApiResponse(
                onSuccess = { _, pagedItems ->
                    if (pagedItems.isNullOrEmpty()) {
                        logger.error("API 응답 데이터가 없습니다. 이름: ${property.name}")
                        return@handleOpenAssemblyApiResponse null
                    } else {

                        // 여러명 중 우선 전체 날짜 형식으로 정규화하여 비교 시도
                        val mp = pagedItems.firstOrNull { candidate ->
                            val candidateNorm = normalizeBirthDate(candidate.birthDate)
                            val propertyNorm = normalizeBirthDate(property.birthDate)
                            if (candidateNorm != null && propertyNorm != null) {
                                candidateNorm == propertyNorm
                            } else {
                                // 정규화 실패 시 연도만 추출하여 비교
                                val candidateYear = extractYear(candidate.birthDate)
                                val propertyYear = extractYear(property.birthDate)
                                candidateYear != null && propertyYear != null && candidateYear == propertyYear
                            }
                        } ?: return@handleOpenAssemblyApiResponse null

                        return@handleOpenAssemblyApiResponse MPSyncDataModel.toDataModel(mp)
                    }
                },
                onError = { e ->
                    logger.error("에러 코드: (${e.code}) → ${e.message}")
                    return@handleOpenAssemblyApiResponse null
                }
            )
        } catch (e: Exception) {
            logger.error("API 호출/파싱 오류: ${e.message}")
            return null
        }
    }
}