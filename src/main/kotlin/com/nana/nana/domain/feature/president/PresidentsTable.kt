package com.nana.nana.domain.feature.president

import com.nana.nana.domain.enums.Birth
import com.nana.nana.domain.enums.Gender
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

/**
 * 대통령 데이터를 정의하는 Exposed 테이블
 * - presidents: 대통령 정보를 저장하는 테이블
 */
object PresidentsTable : Table("presidents") {

    val id = varchar("id", 30) // 이름_생년월일

    val termStartDate = date("term_start_date").nullable() // 임기 시작 날짜

    val termEndDate = date("term_end_date").nullable() // 임기 종료 날짜

    val name = varchar("name", 100) // 대통령명

    val nameInChinese = varchar("name_chn", 30).nullable() // 대통령 한자명

    val nameInEnglish = varchar("name_eng", 30).nullable() // 대통령 영문명

    val birthDivision = enumerationByName("birth_division_code", 3, Birth::class) // 생일 구분 코드 (양/음)

    val birthDate = date("birth_date").nullable() // 생일

    val positionName = varchar("position", 100).nullable() // 직책명

    val partyName = varchar("party_name", 100) // 정당명

    val electoralDistrictName = varchar("constituency_name", 100).nullable() // 선거구명

    val electoralDistrictDivisionName = varchar("constituency_type", 100).nullable() // 선거구 구분명

    val committeeName = text("committee_name").nullable() // 소속 상임위원회명

    val reElectionDivisionName = varchar("re_election_status", 100).nullable() // 재선 구분명

    val electionEraco = varchar("election_number", 100).nullable() // 당선 대수

    val gender = enumerationByName("gender", 2, Gender::class).nullable() // 성별 (남/여)

    val officePhoneNumber = varchar("phone_number", 100).nullable() // 사무실 전화번호

    val officeEmail = varchar("email", 100).nullable() // 대통령 이메일

    val officialWebsiteUrl = varchar("homepage_url", 255).nullable() // 대통령 홈페이지 URL

    val aideName = text("aides").nullable() // 보좌관 목록

    val chiefSecretaryName = text("chief_secretary").nullable() // 비서관 목록

    val secretaryName = text("secretaries").nullable() // 비서 목록

    val careerHistory = text("career_history").nullable() // 약력

    val officeRoomNumber = varchar("office_room_number", 100).nullable() // 사무실 호실

    val profilePictureUrl = varchar("profile_picture_url", 255).nullable() // 프로필 사진 URL

    override val primaryKey = PrimaryKey(id) // Primary Key 설정
}