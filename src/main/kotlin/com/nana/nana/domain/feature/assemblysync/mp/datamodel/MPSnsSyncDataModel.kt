package com.nana.nana.domain.feature.assemblysync.mp.datamodel

import com.nana.nana.domain.feature.assemblysync.mp.apiclient.json.SyncMPSnsJsonProperty
import kotlinx.serialization.Serializable

@Serializable
data class MPSnsSyncDataModel(
    val id: String,
    val xUrl: String,
    val facebookUrl: String? = null,
    val youtubeUrl: String? = null,
    val blogUrl: String? = null,
) {
    companion object {
        fun toDataModel(mp: MPSyncDataModel, sns: SyncMPSnsJsonProperty): MPSnsSyncDataModel {
            return MPSnsSyncDataModel(
                id = mp.id,
                xUrl = sns.xUrl,
                facebookUrl = sns.facebookUrl,
                youtubeUrl = sns.youtubeUrl,
                blogUrl = sns.blogUrl
            )
        }
    }
}