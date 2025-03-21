package com.nana.nana.domain.feature.translation.openai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GPTResponse(
    @SerialName("choices")
    val choices: List<Choice>
)

@Serializable
data class Choice(
    @SerialName("message")
    val message: Message
)

@Serializable
data class Message(
    @SerialName("content")
    val content: String
)