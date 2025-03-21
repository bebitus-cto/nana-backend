package com.nana.nana.domain.feature.auth.datamodel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleTokenInfo(
    @SerialName("email")
    val email: String? = null,
    @SerialName("name")
    val name: String? = null,
)