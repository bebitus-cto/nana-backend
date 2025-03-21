package com.nana.nana.domain.feature.assemblysync.mp.apiclient.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncMPJsonProperty(
    @SerialName("HG_NM")
    val name: String,
    @SerialName("BTH_DATE")
    val birthDate: String,
    @SerialName("HJ_NM")
    val nameInChinese: String? = null,
    @SerialName("ENG_NM")
    val nameInEnglish: String? = null
)