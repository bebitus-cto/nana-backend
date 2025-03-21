package com.nana.nana.domain.feature.assemblysync.lp.response

import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.ResultOk.PROGRESS_LEGISLATION
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblyAPICommonKey.RESULT
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblyBaseApiResponse
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblyResult
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblySuccessResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProgressLegisApiResponse(
    @SerialName(PROGRESS_LEGISLATION)
    override val successResult: List<OpenAssemblySuccessResponse<ProgressLegisResponse>>? = null,

    @SerialName(RESULT)
    override val errorResult: OpenAssemblyResult? = null
) : OpenAssemblyBaseApiResponse<ProgressLegisResponse>()

@Serializable
data class ProgressLegisResponse(

    @SerialName("BILL_NO")
    val id: String,

    @SerialName("BILL_ID")
    val extraId: String,

    @SerialName("BILL_NAME")
    val name: String,

    @SerialName("PROPOSER_KIND_CD")
    val proposerKindCd: String? = null,

    @SerialName("CURR_COMMITTEE")
    val currCommittee: String? = null,

    @SerialName("NOTI_ED_DT")
    val notiEndDate: String? = null,

    @SerialName("LINK_URL")
    val linkUrl: String? = null,

    @SerialName("PROPOSER")
    val proposer: String? = null,

    @SerialName("CURR_COMMITTEE_ID")
    val currCommitteeId: String? = null
)
