package com.nana.nana.domain.feature.assemblysync.pr.response

import com.nana.nana.domain.feature.assemblysync.apihelper.OpenAssemblyAPICommonKey.ITEMS
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 존재하지 않고 json파일 생성
 */
@Serializable
data class PresidentApiResponse(
    @SerialName(ITEMS)
    val presidents: List<PrResponse>? = null
)

@Serializable
data class PrResponse(

    @SerialName("NAAS_NM")
    val name: String, // 대통령 이름

    @SerialName("TERM_START_DT")
    val termStartDate: String,

    @SerialName("TERM_END_DT")
    val termEndDate: String,

    @SerialName("BIRDY_DT")
    val birthDate: String? = null, // 생년월일

    @SerialName("DTY_NM")
    val positionName: String? = null, // 직위

    @SerialName("PLPT_NM")
    val partyName: String, // 정당명

    @SerialName("ELECD_NM")
    val electoralDistrictName: String? = null, // 선거구명

    @SerialName("ELECD_DIV_NM")
    val electoralDistrictDivisionName: String? = null, // 선거구 구분

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
    val profilePictureUrl: String? = null // 프로필 사진 URL
)
