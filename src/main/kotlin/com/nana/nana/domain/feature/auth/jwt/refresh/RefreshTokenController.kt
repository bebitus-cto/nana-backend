package com.nana.nana.domain.feature.auth.jwt.refresh

import com.nana.nana.domain.feature.auth.google.AuthUserRepository
import com.nana.nana.domain.feature.auth.jwt.JwtTokenProvider
import com.nana.nana.domain.feature.auth.jwt.refresh.datamodel.RefreshTokenRequest
import com.nana.nana.domain.feature.auth.jwt.refresh.datamodel.RefreshTokenResponse
import com.nana.nana.domain.feature.auth.user.UserDataModel
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class RefreshTokenController(
    private val jwtTokenProvider: JwtTokenProvider,
    private val authUserRepository: AuthUserRepository
) {

    private val logger = LoggerFactory.getLogger(RefreshTokenController::class.java)

    @PostMapping("/refresh")
    suspend fun refreshToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<RefreshTokenResponse> {
        logger.info("리프레쉬 토큰 갱신 요청: ${request.refreshToken}")
        if (!jwtTokenProvider.validateToken(request.refreshToken)) {
            logger.error("리프레쉬 토큰 검증 실패")
            return ResponseEntity.status(401).build()
        }

        // 리프레쉬 토큰에서 사용자 ID 추출 (토큰의 subject 값)
        val userIdStr = jwtTokenProvider.getUserId(request.refreshToken)
        val userId = userIdStr.toLongOrNull()
        if (userId == null) {
            logger.error("토큰에서 사용자 ID 추출 실패")
            return ResponseEntity.status(401).build()
        }

        // 사용자 정보 조회
        val user: UserDataModel = authUserRepository.findById(userId)
            ?: return ResponseEntity.status(401).build()

        // 새로운 액세스 토큰과 리프레쉬 토큰 생성
        val newAccessToken = jwtTokenProvider.generateAccessToken(user)
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(user)
        // DB에 새로운 리프레쉬 토큰 저장
        authUserRepository.saveRefreshToken(user.id, newRefreshToken)

        logger.info("토큰 갱신 성공: 새로운 토큰 발급")
        return ResponseEntity.ok(
            RefreshTokenResponse(
                accessToken = newAccessToken,
                refreshToken = newRefreshToken
            )
        )
    }
}