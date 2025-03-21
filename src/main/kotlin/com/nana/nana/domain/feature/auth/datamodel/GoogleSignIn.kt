package com.nana.nana.domain.feature.auth.datamodel

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonInclude(JsonInclude.Include.ALWAYS)
data class GoogleSignUpRequest(
    val idToken: String,
    val photoUrl: String? = null
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonInclude(JsonInclude.Include.ALWAYS)
data class GoogleSignUpResponse(
    val accessToken: String,
    val refreshToken: String,
)