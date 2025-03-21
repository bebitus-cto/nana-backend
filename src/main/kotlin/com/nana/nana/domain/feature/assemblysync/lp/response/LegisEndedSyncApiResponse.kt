package com.nana.nana.domain.feature.assemblysync.lp.response

import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.ResultOk.ENDED_LEGISLATION
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblyAPICommonKey.RESULT
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblyBaseApiResponse
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblyResult
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblySuccessResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EndedLegisApiResponse(
    @SerialName(ENDED_LEGISLATION)
    override val successResult: List<OpenAssemblySuccessResponse<EndedLegisResponse>>? = null,

    @SerialName(RESULT)
    override val errorResult: OpenAssemblyResult? = null
) : OpenAssemblyBaseApiResponse<EndedLegisResponse>()

@Serializable
data class EndedLegisResponse(

    @SerialName("BILL_NO")
    val id: String,

    @SerialName("BILL_ID")
    val extraId: String,

    @SerialName("BILL_NAME")
    val name: String,

    @SerialName("PROPOSER_KIND_CD")
    val proposerKindCd: String? = null,

    @SerialName("PROPOSER")
    val proposer: String? = null,

    @SerialName("CURR_COMMITTEE")
    val currCommittee: String? = null,

    @SerialName("NOTI_ED_DT")
    val noticeEndDate: String? = null,

    @SerialName("LINK_URL")
    val linkUrl: String? = null,

    @SerialName("CURR_COMMITTEE_ID")
    val currCommitteeId: String? = null
)
