package com.nana.nana.domain.feature.assemblysync.pr.datamodel

import com.nana.nana.domain.enums.Gender
import com.nana.nana.domain.feature.assemblysync.pr.response.PrResponse
import com.nana.nana.util.DateParser
import java.time.LocalDate

data class PrSyncDataModel(
    val id: String,
    val name: String? = null, // 대통령 이름
    val termStartDate: LocalDate? = null,
    val termEndDate: LocalDate? = null,
    val birthDate: LocalDate? = null, // 생년월일
    val positionName: String? = null, // 직위
    val partyName: String? = null, // 정당명
    val electoralDistrictName: String? = null, // 선거구명
    val electoralDistrictDivisionName: String? = null, // 선거구 구분
    val committeeName: String? = null, // 위원회명
    val reElectionStatus: String? = null, // 재선 여부
    val electionEraco: String? = null, // 당선 대수
    val gender: Gender? = null, // 성별
    val officePhoneNumber: String? = null, // 전화번호
    val officeEmailAddress: String? = null, // 이메일
    val officialWebsiteUrl: String? = null, // 홈페이지 URL
    val history: String? = null, // 경력
    val officeRoomNumber: String? = null, // 사무실 호실
    val profilePictureUrl: String? = null // 프로필 사진 URL
) {
    companion object {

        fun toDataModel(
            rawResponse: PrResponse
        ): PrSyncDataModel = with(rawResponse) {
            PrSyncDataModel(
                id = "${rawResponse.name}_${DateParser.toLocalDate(rawResponse.birthDate)}",
                name = rawResponse.name,
                termStartDate = DateParser.toLocalDate(rawResponse.termStartDate),
                termEndDate = DateParser.toLocalDate(rawResponse.termEndDate),
                birthDate = DateParser.toLocalDate(rawResponse.birthDate),
                positionName = rawResponse.positionName,
                partyName = rawResponse.partyName,
                electoralDistrictName = rawResponse.electoralDistrictName,
                electoralDistrictDivisionName = rawResponse.electoralDistrictDivisionName,
                committeeName = rawResponse.committeeName,
                reElectionStatus = rawResponse.reElectionStatus,
                electionEraco = rawResponse.electionEraco,
                gender = Gender.toGender(rawResponse.gender),
                officePhoneNumber = rawResponse.officePhoneNumber,
                officeEmailAddress = rawResponse.officeEmailAddress,
                officialWebsiteUrl = rawResponse.officialWebsiteUrl,
                history = rawResponse.history,
                officeRoomNumber = rawResponse.officeRoomNumber,
                profilePictureUrl = rawResponse.profilePictureUrl,
            )
        }
    }
}
