package com.nana.nana.domain.feature.assemblysync.mp.response

import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblyResult
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblyAPICommonKey.RESULT
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblySuccessResponse
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.ResultOk.MEMBERS_DETAIL
import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblyBaseApiResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MPApiResponse(
    @SerialName(MEMBERS_DETAIL)
    override val successResult: List<OpenAssemblySuccessResponse<MPResponse>>? = null,
    @SerialName(RESULT)
    override val errorResult: OpenAssemblyResult? = null
) : OpenAssemblyBaseApiResponse<MPResponse>()

@Serializable
data class MPResponse(
    @SerialName("NAAS_CD")
    val extraId: String, // 국회의원 코드

    @SerialName("NAAS_NM")
    val name: String, // 국회의원 이름

    @SerialName("NAAS_CH_NM")
    val nameInChinese: String? = null, // 한자 이름

    @SerialName("NAAS_EN_NM")
    val nameInEnglish: String? = null, // 영어 이름

    @SerialName("BIRDY_DT")
    val birthDate: String? = null, // 생년월일

    @SerialName("DTY_NM")
    val positionName: String? = null, // 직위

    @SerialName("PLPT_NM")
    val partyName: String, // 정당명

    @SerialName("ELECD_NM")
    val electoralDistrictName: String? = null, // 선거구명

    @SerialName("ELECD_DIV_NM")
    val electoralDistrictType: String? = null, // 선거구 구분

    @SerialName("CMIT_NM")
    val committeeName: String? = null, // 위원회명

    @SerialName("RLCT_DIV_NM")
    val reElectionStatus: String? = null, // 재선 여부

    @SerialName("GTELT_ERACO")
    val electionEraco: String? = null, // 당선 대수

    @SerialName("NTR_DIV")
    val gender: String? = null, // 성별

    @SerialName("NAAS_TEL_NO")
    val officePhoneNumber: String? = null, // 전화번호

    @SerialName("NAAS_EMAIL_ADDR")
    val officeEmailAddress: String? = null, // 이메일

    @SerialName("NAAS_HP_URL")
    val officialWebsiteUrl: String? = null, // 홈페이지 URL

    @SerialName("BRF_HST")
    val history: String? = null, // 경력

    @SerialName("OFFM_RNUM_NO")
    val officeRoomNumber: String? = null, // 사무실 호실

    @SerialName("NAAS_PIC")
    val profilePictureUrl: String? = null, // 프로필 사진 URL
)


