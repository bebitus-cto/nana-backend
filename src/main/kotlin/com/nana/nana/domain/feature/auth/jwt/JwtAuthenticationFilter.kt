package com.nana.nana.domain.feature.auth.jwt

import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.web.servlet.DispatcherType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    @Throws(ServletException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.substring(7)
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    val authentication = jwtTokenProvider.getAuthentication(token)
                    SecurityContextHolder.getContext().authentication = authentication
                }
            } catch (ex: ExpiredJwtException) {
                logger.info("액세스 토큰 만료: ${ex.message}")
            } catch (ex: Exception) {
                logger.error("JWT 처리 중 에러 발생: ${ex.message}")
            }
        }
        filterChain.doFilter(request, response)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        if (request.dispatcherType == jakarta.servlet.DispatcherType.FORWARD) {
            logger.debug("DispatcherType is FORWARD, skipping filter")
            return true
        }
        val path = request.requestURI.removePrefix(request.contextPath)
        val shouldSkip =
            path.startsWith("/auth/") || path.startsWith("/WEB-INF/views/auth/") || path.startsWith("/phone_popup2")
        logger.debug("shouldNotFilter: $shouldSkip")
        return shouldSkip
    }
}