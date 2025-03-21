package com.nana.nana.domain.feature.assemblysync.mp.apiclient.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncMPSnsJsonProperty(
    @SerialName("HG_NM")
    val name: String,
    @SerialName("T_URL")
    val xUrl: String,
    @SerialName("F_URL")
    val facebookUrl: String? = null,
    @SerialName("Y_URL")
    val youtubeUrl: String? = null,
    @SerialName("B_URL")
    val blogUrl: String? = null,
)