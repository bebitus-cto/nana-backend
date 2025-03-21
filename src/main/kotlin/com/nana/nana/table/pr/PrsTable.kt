package com.nana.nana.table.pr

import com.nana.nana.domain.enums.Gender
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

abstract class PrsTable(name: String) : Table(name) {

    val id = varchar("id", 30) // 이름_생년월일

    val termStartDate = date("term_start_date").nullable() // 임기 시작 날짜

    val termEndDate = date("term_end_date").nullable() // 임기 종료 날짜

    val name = varchar("name", 500).nullable() // 대통령명

    val birthDate = date("birth_date").nullable() // 생일

    val positionName = varchar("position", 500).nullable() // 직책명

    val partyName = varchar("party_name", 500).nullable() // 정당명

    val electoralDistrictName = varchar("electoral_district_name", 500).nullable() // 선거구명

    val electoralDistrictType = varchar("electoral_district_type", 500).nullable() // 선거구 구분명

    val committeeName = text("committee_name").nullable() // 소속 상임위원회명

    val reElectionStatus = varchar("re_election_status", 500).nullable() // 재선 구분명

    val electionEraco = varchar("election_number", 500).nullable() // 당선 대수

    val gender = enumerationByName("gender", 2, Gender::class).nullable() // 성별 (남/여)

    val officePhoneNumber = varchar("phone_number", 500).nullable() // 사무실 전화번호

    val officeEmailAddress = varchar("email", 500).nullable() // 대통령 이메일

    val officialWebsiteUrl = varchar("homepage_url", 500).nullable() // 대통령 홈페이지 URL

    val careerHistory = text("career_history").nullable() // 약력

    val officeRoomNumber = varchar("office_room_number", 500).nullable() // 사무실 호실

    val profilePictureUrl = varchar("profile_picture_url", 500).nullable() // 프로필 사진 URL

    override val primaryKey = PrimaryKey(id) // Primary Key 설정
}

object PrsKoTable : PrsTable("presidents_ko")
object PrsEnTable : PrsTable("presidents_en")
object PrsJaTable : PrsTable("presidents_ja")
object PrsZhTable : PrsTable("presidents_zh")

fun getPrsTable(languageCode: String? = "ko"): PrsTable {
    return when (languageCode) {
        "ko" -> PrsKoTable
        "en" -> PrsEnTable
        "ja" -> PrsJaTable
        "zh" -> PrsZhTable
        else -> throw IllegalArgumentException("지원되지 않는 언어 코드: $languageCode")
    }
}