package com.nana.nana.domain.feature.assemblysync.lp.response

import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.ResultOk.PENDING_LAW_PROPOSAL
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblyAPICommonKey.RESULT
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblyBaseApiResponse
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblyResult
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblySuccessResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PendingLPApiResponse(
    @SerialName(PENDING_LAW_PROPOSAL)
    override val successResult: List<OpenAssemblySuccessResponse<PendingLPResponse>>? = null,

    @SerialName(RESULT)
    override val errorResult: OpenAssemblyResult? = null
) : OpenAssemblyBaseApiResponse<PendingLPResponse>()

@Serializable
data class PendingLPResponse(

    @SerialName("BILL_NO")
    val id: String,

    @SerialName("BILL_ID")
    val extraId: String,

    @SerialName("BILL_NAME")
    val name: String,

    @SerialName("PROPOSER")
    val proposer: String? = null,

    @SerialName("PROPOSER_KIND")
    val proposerKind: String? = null,

    @SerialName("PROPOSE_DT")
    val proposeDate: String? = null,

    @SerialName("CURR_COMMITTEE_ID")
    val currCommitteeId: String? = null,

    @SerialName("CURR_COMMITTEE")
    val currCommittee: String? = null,

    @SerialName("COMMITTEE_DT")
    val committeeDate: String? = null,

    @SerialName("LINK_URL")
    val linkUrl: String? = null,

    @SerialName("CMT_PROC_DT")
    val cmtProcDate: String? = null,

    @SerialName("CMT_PROC_RESULT_CD")
    val cmtProcResultCd: String? = null,

    @SerialName("RST_MONA_CD")
    val rstMonaCd: String? = null,

    @SerialName("LAW_PRESENT_DT")
    val lawPresentDate: String? = null,

    @SerialName("LAW_PROC_DT")
    val lawProcDate: String? = null,

    @SerialName("LAW_PROC_RESULT_CD")
    val lawProcResultCd: String? = null,

    @SerialName("RST_PROPOSER")
    val rstProposer: String? = null,

    @SerialName("CMT_PRESENT_DT")
    val cmtPresentDate: String? = null,

    @SerialName("LAW_SUBMIT_DT")
    val lawSubmitDate: String? = null
)