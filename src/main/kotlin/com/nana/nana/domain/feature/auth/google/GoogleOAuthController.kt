package com.nana.nana.domain.feature.auth.google

import com.nana.nana.domain.feature.auth.datamodel.GoogleSignUpRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class GoogleOAuthController(private val googleOAuthService: GoogleOAuthService) {

    private val logger: Logger = LoggerFactory.getLogger(GoogleOAuthController::class.java)

    @PostMapping("/google")
    suspend fun googleLogin(@RequestBody request: GoogleSignUpRequest): ResponseEntity<Any> {
        logger.info("로그인 요청 수신: $request")
        val response = googleOAuthService.verifyIdToken(request.idToken, request.photoUrl)
        return ResponseEntity.ok(response)
    }
}