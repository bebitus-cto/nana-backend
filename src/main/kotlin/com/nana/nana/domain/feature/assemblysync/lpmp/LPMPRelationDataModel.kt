package com.nana.nana.domain.feature.assemblysync.lpmp

import com.nana.nana.domain.enums.ProposerRole

data class LPMPRelationDataModel(
    val lpId: String,
    val mpId: String,
    val role: ProposerRole
)