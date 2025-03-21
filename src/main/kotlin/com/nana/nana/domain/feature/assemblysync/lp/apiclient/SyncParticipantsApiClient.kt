package com.nana.nana.domain.feature.assemblysync.lp.apiclient

import com.nana.nana.config.WebClientConfig.Companion.ASSEMBLY_QUALIFIER
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.Url.PROPOSER_DETAIL_URL
import com.nana.nana.config.ApiConfig.defaultJson
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.PAGE
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.getOpenAssemblyBaseUriBuilder
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.toDecodedUriString
import com.nana.nana.domain.feature.assemblysync.apihelper.handleOpenAssemblyApiResponse
import com.nana.nana.domain.feature.assemblysync.config.SyncConfig.CURRENT_MP_ERACO
import com.nana.nana.domain.feature.assemblysync.lp.response.ProposerDetailApiResponse
import com.nana.nana.domain.feature.assemblysync.lpmp.ParticipantSyncDataModel
import com.nana.nana.domain.feature.assemblysync.mp.datamodel.MPSyncDataModel
import com.nana.nana.domain.feature.log.LogLPSyncRepository
import com.nana.nana.util.WebParser
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class SyncParticipantsApiClient(
    @Qualifier(ASSEMBLY_QUALIFIER) private val webClient: WebClient,
    private val webParser: WebParser,
    private val logLPSyncRepository: LogLPSyncRepository
) {

    private val logger: Logger = LoggerFactory.getLogger(SyncParticipantsApiClient::class.java)

    /**
     * API를 호출하여 의안의 MEMBER_LIST URL을 추출하는 함수
     * @param id 의안 번호
     * @return MEMBER_LIST URL
     * @retuern null인 경우는 발의자가 없고 '장'이 하는 경우
     */
    suspend fun fetchParticipants(
        id: String,
        extraId: String,
        proposerKind: String?,
        existigMPs: Map<String, MPSyncDataModel>,
    ): ParticipantSyncDataModel? = coroutineScope {
        try {

            val url = getOpenAssemblyBaseUriBuilder(PROPOSER_DETAIL_URL)
                .queryParam("AGE", CURRENT_MP_ERACO)
                .queryParam("BILL_NO", id)
                .toDecodedUriString()

            val response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String::class.java)
                .awaitSingleOrNull()
                .orEmpty()
                .let { defaultJson.decodeFromString<ProposerDetailApiResponse>(it) }

            logger.debug("fetchParticipants 응답: $response")

            response.handleOpenAssemblyApiResponse(
                onSuccess = { _, pagedItems ->
                    val memberListUrl = pagedItems?.firstOrNull()?.memberListUrl
                    logger.debug("발의목록 url: $memberListUrl")
                    val result = webParser.extractParticipants(id, memberListUrl, existigMPs)
                    return@handleOpenAssemblyApiResponse result
                },
                onError = { e ->
                    if (proposerKind?.contains("의원") == true) {
                        try {
                            val result =
                                webParser.extractParticipants(id, "${PAGE.COACTOR}${extraId}", existigMPs)

                            return@handleOpenAssemblyApiResponse result
                        } catch (e: Exception) {
                            newSuspendedTransaction {
                                logLPSyncRepository
                                    .logLPSyncError("의안 대표발의자 공동발의자 없음? => proposerKind:$proposerKind $e", id)
                            }
                            return@handleOpenAssemblyApiResponse null
                        }
                    } else {
                        return@handleOpenAssemblyApiResponse null
                    }
                }
            )
        } catch (e: Exception) {
            if (proposerKind?.contains("의원") == true) {
                newSuspendedTransaction {
                    logLPSyncRepository.logLPSyncError("의안 대표발의자 공동발의자 오류? $e => proposerKind: $proposerKind", id)
                }
            }
            null
        }
    }
}