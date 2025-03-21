package com.nana.nana.domain.feature.assemblysync.lpmp

import com.nana.nana.domain.feature.assemblysync.lp.datamodel.LPSyncDataModel

data class PartyRankDataModel(
    val lpSyncDataModel: LPSyncDataModel,
    val leadingParty: String?,
    val secondParty: String?,
    val thirdParty: String?
)