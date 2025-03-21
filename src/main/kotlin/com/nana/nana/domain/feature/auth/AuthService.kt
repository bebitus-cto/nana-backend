package com.nana.nana.domain.feature.auth

import org.springframework.stereotype.Service

@Service
class AuthService(
    private val repository: AuthRepository
)