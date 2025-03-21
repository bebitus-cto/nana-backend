package com.nana.nana.domain.feature.translation.google

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LibreTransRequest(
    @SerialName("q")
    val q: String,
    @SerialName("source")
    val source: String,
    @SerialName("target")
    val target: String,
    @SerialName("format")
    val format: String = "text"
)

@Serializable
data class LibreTransResponse(
    @SerialName("translatedText")
    val translatedTexts: String = "",
    @SerialName("error")
    val error: String? = null
)