package com.nana.nana.domain.feature.assemblysync.lp.response

import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.ResultOk.LAW_PROPOSAL_FULL_DETAIL
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblyAPICommonKey.RESULT
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblyBaseApiResponse
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblyResult
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblySuccessResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LPDetailApiResponse(
    @SerialName(LAW_PROPOSAL_FULL_DETAIL)
    override val successResult: List<OpenAssemblySuccessResponse<LPDetailResponse>>? = null,

    @SerialName(RESULT)
    override val errorResult: OpenAssemblyResult? = null
) : OpenAssemblyBaseApiResponse<LPDetailResponse>()

@Serializable
data class LPDetailResponse(

    @SerialName("BILL_NO")
    val id: String,

    @SerialName("BILL_ID")
    val extraId: String,

    @SerialName("BILL_KND")
    val kind: String,

    @SerialName("BILL_NM")
    val name: String,

    @SerialName("PPSR_KND")
    val proposerKind: String? = null,

    @SerialName("PPSR_NM")
    val proposerOverview: String? = null,

    @SerialName("PPSL_SESS")
    val proposeSession: String? = null,

    @SerialName("PPSL_DT")
    val proposalDate: String,

    @SerialName("JRCMIT_NM")
    val committeeName: String? = null,

    @SerialName("JRCMIT_CMMT_DT")
    val committeeCommitDate: String? = null,

    @SerialName("JRCMIT_PRSNT_DT")
    val committeePresentationDate: String? = null,

    @SerialName("JRCMIT_PROC_DT")
    val committeeProcessingDate: String? = null,

    @SerialName("JRCMIT_PROC_RSLT")
    val committeeResult: String? = null,

    @SerialName("LAW_CMMT_DT")
    val judicialReferralDate: String? = null,

    @SerialName("LAW_PRSNT_DT")
    val judicialPresentationDate: String? = null,

    @SerialName("LAW_PROC_DT")
    val judicialProcessingDate: String? = null,

    @SerialName("LAW_PROC_RSLT")
    val judicialResult: String? = null,

    @SerialName("RGS_PRSNT_DT")
    val plenaryPresentationDate: String? = null,

    @SerialName("RGS_RSLN_DT")
    val plenaryResolutionDate: String? = null,

    @SerialName("RGS_CONF_NM")
    val plenaryConferenceName: String? = null,

    @SerialName("RGS_CONF_RSLT")
    val plenaryResult: String? = null,

    @SerialName("GVRN_TRSF_DT")
    val governmentTransferDate: String? = null,

    @SerialName("PROM_LAW_NM")
    val promulgationLawName: String? = null,

    @SerialName("PROM_DT")
    val promulgationDate: String? = null,

    @SerialName("PROM_NO")
    val promulgationNo: String? = null,

    @SerialName("LINK_URL")
    val linkUrl: String? = null,
) {

    companion object {

    }
}