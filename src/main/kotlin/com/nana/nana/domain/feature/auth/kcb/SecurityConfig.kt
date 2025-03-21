package com.nana.nana.domain.feature.auth.kcb

import com.nana.nana.domain.feature.auth.jwt.JwtAuthenticationEntryPoint
import com.nana.nana.domain.feature.auth.jwt.JwtAuthenticationFilter
import com.nana.nana.domain.feature.auth.jwt.JwtTokenProvider
import jakarta.servlet.DispatcherType
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.RequestMatcher


@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(
                    "/auth/**",
                    "/phone_popup",
                    "/WEB-INF/views/auth/**"
                ).permitAll()
                // 나머지 경로는 인증 필요
                auth.anyRequest().authenticated()
            }
            .csrf { csrf -> csrf.disable() } // CSRF 비활성화
            .sessionManagement { session ->
                // JWT 기반 인증이므로 STATELESS 설정
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            // 인증 실패 시 JwtAuthenticationEntryPoint가 401 에러를 반환하도록 설정
            .exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint(JwtAuthenticationEntryPoint())
            }
            // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 이전에 등록
            .addFilterBefore(
                JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter::class.java
            )
            // 기본 httpBasic 설정 (필요에 따라 커스터마이징 가능)
            .httpBasic(Customizer.withDefaults())

        return http.build()
    }
}