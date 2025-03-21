package com.nana.nana.domain.feature.assemblysync.mp.datamodel

import com.nana.nana.domain.enums.Gender
import com.nana.nana.domain.feature.assemblysync.mp.response.MPResponse
import com.nana.nana.util.DateParser
import java.time.LocalDate

data class MPSyncDataModel(
    val id: String,
    val extraId: String? = null,
    val name: String, // 국회의원 이름
    val nameInChinese: String? = null,
    val nameInEnglish: String? = null,
    val birthDate: LocalDate? = null, // 생년월일
    val positionName: String? = null, // 직위
    val partyName: String? = null, // 정당명
    val electoralDistrictName: String? = null, // 선거구명
    val electoralDistrictType: String? = null, // 선거구 구분
    val committeeName: String? = null, // 위원회명
    val reElectionStatus: String? = null, // 재선 여부
    val electionEraco: String? = null, // 당선 대수
    val gender: Gender? = null, // 성별
    val officePhoneNumber: String? = null, // 전화번호
    val officeEmailAddress: String? = null, // 이메일
    val officialWebsiteUrl: String? = null, // 홈페이지 URL
    val history: String? = null, // 경력
    val officeRoomNumber: String? = null, // 사무실 호실
    val profilePictureUrl: String? = null, // 프로필 사진 URL
) {
    companion object {
        fun toDataModel(
            rawResponse: MPResponse
        ): MPSyncDataModel = with(rawResponse) {
            MPSyncDataModel(
                id = "${rawResponse.name}_${DateParser.toLocalDate(rawResponse.birthDate)}",
                extraId = extraId,
                name = name,
                nameInEnglish = rawResponse.nameInEnglish,
                nameInChinese = rawResponse.nameInChinese,
                birthDate = DateParser.toLocalDate(rawResponse.birthDate),
                positionName = rawResponse.positionName,
                partyName = rawResponse.partyName,
                electoralDistrictName = rawResponse.electoralDistrictName,
                electoralDistrictType = rawResponse.electoralDistrictType,
                committeeName = rawResponse.committeeName,
                reElectionStatus = rawResponse.reElectionStatus,
                electionEraco = rawResponse.electionEraco,
                gender = Gender.toGender(rawResponse.gender),
                officePhoneNumber = rawResponse.officePhoneNumber,
                officeEmailAddress = rawResponse.officeEmailAddress,
                officialWebsiteUrl = rawResponse.officialWebsiteUrl,
                history = rawResponse.history,
                officeRoomNumber = rawResponse.officeRoomNumber,
                profilePictureUrl = rawResponse.profilePictureUrl
            )
        }
    }
}