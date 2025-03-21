package com.nana.nana.domain.feature.assemblysync.lp.response

import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.ResultOk.PROPOSER_DETAIL
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblyAPICommonKey.RESULT
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblyBaseApiResponse
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblyResult
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblySuccessResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProposerDetailApiResponse(
    @SerialName(PROPOSER_DETAIL)
    override val successResult: List<OpenAssemblySuccessResponse<ProposerDetailResponse>>? = null,

    @SerialName(RESULT)
    override val errorResult: OpenAssemblyResult? = null
) : OpenAssemblyBaseApiResponse<ProposerDetailResponse>()

@Serializable
data class ProposerDetailResponse(

    @SerialName("BILL_NO")
    val id: String,

    @SerialName("BILL_ID")
    val extraId: String,

    @SerialName("BILL_NAME")
    val name: String,

    @SerialName("COMMITTEE")
    val committee: String? = null,

    @SerialName("PROPOSE_DT")
    val proposeDate: String? = null,

    @SerialName("PROC_RESULT")
    val procResult: String? = null,

    @SerialName("AGE")
    val age: Int? = null,

    @SerialName("DETAIL_LINK")
    val detailLink: String? = null,

    @SerialName("PROPOSER")
    val proposer: String? = null,

    @SerialName("MEMBER_LIST")
    val memberListUrl: String? = null,

    @SerialName("LAW_PROC_DT")
    val lawProcDate: String? = null,

    @SerialName("LAW_PRESENT_DT")
    val lawPresentDate: String? = null,

    @SerialName("LAW_SUBMIT_DT")
    val lawSubmitDate: String? = null,

    @SerialName("CMT_PROC_RESULT_CD")
    val cmtProcResultCd: String? = null,

    @SerialName("CMT_PROC_DT")
    val cmtProcDate: String? = null,

    @SerialName("CMT_PRESENT_DT")
    val cmtPresentDate: String? = null,

    @SerialName("COMMITTEE_DT")
    val committeeDate: String? = null,

    @SerialName("PROC_DT")
    val procDate: String? = null,

    @SerialName("COMMITTEE_ID")
    val committeeId: String? = null,

    @SerialName("PUBL_PROPOSER")
    val publProposer: String? = null,

    @SerialName("LAW_PROC_RESULT_CD")
    val lawProcResultCd: String? = null,

    @SerialName("RST_PROPOSER")
    val rstProposer: String? = null
)

