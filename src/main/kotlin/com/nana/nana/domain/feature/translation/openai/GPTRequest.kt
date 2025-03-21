package com.nana.nana.domain.feature.translation.openai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GPTRequest(
    @SerialName("model")
    val model: String = "gpt-4-turbo",
    @SerialName("messages")
    val messages: List<Map<String, String>>,
    @SerialName("temperature")
    val temperature: Double = 0.1
)