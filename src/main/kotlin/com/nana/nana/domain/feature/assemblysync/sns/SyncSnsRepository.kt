package com.nana.nana.domain.feature.assemblysync.sns

import com.nana.nana.domain.feature.assemblysync.mp.apiclient.json.SyncMPSnsJsonProperty
import com.nana.nana.domain.feature.assemblysync.mp.datamodel.MPSnsSyncDataModel
import com.nana.nana.domain.feature.assemblysync.mp.datamodel.MPSyncDataModel
import com.nana.nana.domain.feature.sns.SnsMPTable
import com.nana.nana.domain.feature.sns.SnsMPTable.blogUrl
import com.nana.nana.domain.feature.sns.SnsMPTable.facebookUrl
import com.nana.nana.domain.feature.sns.SnsMPTable.mpId
import com.nana.nana.domain.feature.sns.SnsMPTable.xUrl
import com.nana.nana.domain.feature.sns.SnsMPTable.youtubeUrl
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.batchUpsert
import org.springframework.stereotype.Repository

@Repository
class SyncSnsRepository {

    suspend fun batchUpsertSns(
        dbData: List<MPSyncDataModel>,
        snsMap: Map<String, SyncMPSnsJsonProperty>
    ): List<ResultRow> {
        val snsDataModel = dbData.mapNotNull { mp ->
            snsMap[mp.name]?.let { sns ->
                MPSnsSyncDataModel.toDataModel(mp, sns)
            }
        }

        return SnsMPTable.batchUpsert(snsDataModel) { sns ->
            this[mpId] = sns.id
            this[xUrl] = sns.xUrl
            this[facebookUrl] = sns.facebookUrl
            this[youtubeUrl] = sns.youtubeUrl
            this[blogUrl] = sns.blogUrl
        }
    }

}