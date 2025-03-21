package com.nana.nana.domain.feature.president.response

import com.nana.nana.domain.data.OpenAssemblyAPICommonKey.ITEMS
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// API 존재 안함
@Serializable
data class PresidentApiResponse(
    @SerialName(ITEMS)
    val presidents: List<PresidentResponse>? = null
)

@Serializable
data class PresidentResponse(
    @SerialName("NAAS_CD")
    val code: String, // 대통령 코드(국회의원 코드랑 동일 추정)

    @SerialName("NAAS_NM")
    val name: String, // 대통령 이름

    @SerialName("TERM_START_DT")
    val termStartDate: String,

    @SerialName("TERM_END_DT")
    val termEndDate: String,

    @SerialName("NAAS_CH_NM")
    val nameInChinese: String? = null, // 한자 이름

    @SerialName("NAAS_EN_NM")
    val nameInEnglish: String? = null, // 영어 이름

    @SerialName("BIRDY_DIV_CD")
    val birthDateDivision: String, // 생년월일 구분 (양/음)

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
    val reElectionDivisionName: String? = null, // 재선 여부

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

    @SerialName("AIDE_NM")
    val aideName: String? = null, // 보좌관

    @SerialName("CHF_SCRT_NM")
    val chiefSecretaryName: String? = null, // 수석 비서관

    @SerialName("SCRT_NM")
    val secretaryName: String? = null, // 비서관

    @SerialName("BRF_HST")
    val careerHistory: String? = null, // 경력

    @SerialName("OFFM_RNUM_NO")
    val officeRoomNumber: String? = null, // 사무실 호실

    @SerialName("NAAS_PIC")
    val profilePictureUrl: String? = null // 프로필 사진 URL
)
