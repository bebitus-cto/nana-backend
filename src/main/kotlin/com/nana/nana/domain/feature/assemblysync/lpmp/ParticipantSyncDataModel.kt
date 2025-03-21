package com.nana.nana.domain.feature.assemblysync.lpmp

data class ParticipantSyncDataModel(
    val proposerOverview: String? = null,
    val mainProposers: List<String>? = null,
    val coProposers: List<String>? = null,
    val supporters: List<String>? = null,
)