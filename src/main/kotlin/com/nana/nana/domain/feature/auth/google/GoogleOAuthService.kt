package com.nana.nana.domain.feature.auth.google

import com.nana.nana.domain.enums.JoinMethod
import com.nana.nana.config.ApiConfig.defaultJson
import com.nana.nana.domain.feature.auth.datamodel.GoogleSignUpResponse
import com.nana.nana.domain.feature.auth.datamodel.GoogleTokenInfo
import com.nana.nana.domain.feature.auth.jwt.JwtTokenProvider
import com.nana.nana.domain.feature.auth.user.UserDataModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class GoogleOAuthService(
    private val authUserRepository: AuthUserRepository,
    @Qualifier("googleOAuthWebClient")
    private val webClient: WebClient,
    private val jwtTokenProvider: JwtTokenProvider
) {

    private val logger = LoggerFactory.getLogger(GoogleOAuthService::class.java)

    suspend fun verifyIdToken(
        idToken: String,
        photoUrl: String? = null
    ): GoogleSignUpResponse = newSuspendedTransaction(Dispatchers.IO) {

        val tokenInfoResponse = webClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/tokeninfo")
                    .queryParam("id_token", idToken)
                    .build()
            }
            .retrieve()
            .bodyToMono(String::class.java)
            .awaitSingleOrNull() ?: throw IllegalArgumentException("유효하지 않은 GoogleTokenInfo")

        val googleTokenInfo = defaultJson.decodeFromString<GoogleTokenInfo>(tokenInfoResponse)
        logger.info("구글 토큰 검증 성공: $googleTokenInfo")

        // 이메일이 없거나 공백이면 예외 발생
        if (googleTokenInfo.email.isNullOrBlank()) {
            throw IllegalArgumentException("Google 토큰에 이메일 값이 존재하지 않습니다.")
        }

        // 사용자 조회 또는 신규 생성
        // 사용자 조회 후 존재하면 예외 발생, 없으면 신규 사용자 생성
        val existingUser = authUserRepository.findByEmail(googleTokenInfo.email)
        if (existingUser != null) {
            throw IllegalArgumentException("이미 존재하는 사용자입니다.")
        }
        val user = createUser(googleTokenInfo, photoUrl)

        // JWT 액세스 토큰과 리프레쉬 토큰 발급
        val accessToken = jwtTokenProvider.generateAccessToken(user)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user)
        logger.info("JWT 토큰 발급 완료 - accessToken: $accessToken, refreshToken: $refreshToken")

        // 리프레쉬 토큰 DB 저장
        authUserRepository.saveRefreshToken(user.id, refreshToken)

        GoogleSignUpResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    private suspend fun createUser(googleTokenInfo: GoogleTokenInfo, photoUrl: String?): UserDataModel {
        logger.info("신규 사용자 생성: $googleTokenInfo, 사진: $photoUrl")
        return authUserRepository.createUser(
            email = googleTokenInfo.email,
            name = googleTokenInfo.name,
            profilePictureUrl = photoUrl,
            joinMethod = JoinMethod.GOOGLE
        )
    }
}