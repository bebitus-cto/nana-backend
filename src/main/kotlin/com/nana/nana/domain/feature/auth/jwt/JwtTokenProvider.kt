package com.nana.nana.domain.feature.auth.jwt

import com.nana.nana.domain.feature.auth.user.UserDataModel
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Deserializer
import io.jsonwebtoken.security.Keys
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secretKey: String
) {

    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    // 액세스 토큰 유효기간 (15분)
    private val accessTokenValidityInMilliseconds = 15 * 60 * 1000
    // 리프레쉬 토큰 유효기간 (30일)
    private val refreshTokenValidityInMilliseconds = 30L * 24 * 60 * 60 * 1000

    private val key = Keys.hmacShaKeyFor(secretKey.toByteArray(Charsets.UTF_8))

    /**
     * 사용자 정보를 기반으로 JWT 액세스 토큰을 생성합니다.
     */
    fun generateAccessToken(user: UserDataModel): String {
        val now = Date()
        val validity = Date(now.time + accessTokenValidityInMilliseconds)

        return Jwts.builder()
            .setSubject(user.id.toString())
            .claim("email", user.email)
            .claim("name", user.name)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }


    /**
     * 사용자 정보를 기반으로 JWT 리프레쉬 토큰을 생성합니다.
     */
    fun generateRefreshToken(user: UserDataModel): String {
        val now = Date()
        val validity = Date(now.time + refreshTokenValidityInMilliseconds)

        return Jwts.builder()
            .setSubject(user.id.toString())
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }


    /**
     * 전달받은 JWT 토큰의 유효성을 검증합니다.
     * 만료되었거나 위변조된 토큰인 경우 false를 반환합니다.
     */
    fun validateToken(token: String): Boolean {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            val expiration = claims.body.expiration
            if (expiration.before(Date())) {
                logger.info("토큰 만료됨: 만료시간 $expiration")
                false
            } else {
                true
            }
        } catch (e: JwtException) {
            logger.error("토큰 검증 실패: ${e.message}")
            false
        } catch (e: IllegalArgumentException) {
            logger.error("토큰이 비어있음: ${e.message}")
            false
        }
    }

    /**
     * JWT 토큰에서 사용자 정보를 추출하여 Authentication 객체를 생성합니다.
     * 실제 구현에서는 추가 권한 정보 등을 포함할 수 있습니다.
     */
    fun getAuthentication(token: String): Authentication {
        val claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
        val userId = claims.body.subject
        // claim "email" 및 "name"을 활용할 수 있으나, 여기서는 email을 principal로 사용
        val email = claims.body["email"] as? String ?: ""
        val name = claims.body["name"] as? String ?: ""
        // 실제 환경에서는 권한 정보와 함께 UserDetails 객체를 생성합니다.
        val principal = org.springframework.security.core.userdetails.User(email, "", emptyList())
        return UsernamePasswordAuthenticationToken(principal, token, principal.authorities)
    }

    // refresh token으로부터 사용자 ID(토큰의 subject)를 추출합니다.
    fun getUserId(token: String): String {
        val claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
        return claims.body.subject
    }
}