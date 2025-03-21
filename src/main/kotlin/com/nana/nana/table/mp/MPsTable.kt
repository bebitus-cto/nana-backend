package com.nana.nana.table.mp

import com.nana.nana.domain.enums.Gender
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

abstract class MPsTable(name: String) : Table(name) {

    val id = varchar("id", 30) // 이름_생년월일

    val extraId = varchar("extra_id", 20).nullable().uniqueIndex() // 국회의원 코드 (UNIQUE)

    val name = varchar("name", 50).nullable() // 국회의원명

    val birthDate = date("birth_date").nullable() // 생일

    val birthPlace = varchar("birth_place", 300).nullable() // 출생지

    val positionName = varchar("position_name", 500).nullable() // 직책명

    val partyName = varchar("party_name", 500).nullable() // 정당명

    val electoralDistrictName = varchar("electoral_district_name", 500).nullable() // 선거구명

    val electoralDistrictType = varchar("electoral_district_type", 500).nullable() // 선거구 구분명

    val committeeName = text("committee_name").nullable() // 소속 상임위원회명

    val reElectionStatus = varchar("re_election_status", 50).nullable() // 재선 구분명

    val electionEraco = varchar("election_number", 100).nullable() // 당선 대수

    val gender = enumerationByName("gender", 2, Gender::class).nullable() // 성별 (남/여)

    val officePhoneNumber = varchar("phone_number", 100).nullable() // 사무실 전화번호

    val careerHistory = text("career_history").nullable() // 약력

    val educationHistory = text("education_history").nullable() // 학력

    val officeEmail = varchar("email", 100).nullable() // 국회의원 이메일

    val officialWebsiteUrl = varchar("homepage_url", 500).nullable() // 국회의원 홈페이지 URL

    val officeRoomNumber = varchar("office_room_number", 500).nullable() // 의원 사무실 호실

    val profilePictureUrl = varchar("profile_picture_url", 500).nullable() // 프로필 사진 URL

    override val primaryKey = PrimaryKey(id) // Primary Key 설정
}

object MPsKoTable : MPsTable("members_of_parliament_ko")
object MPsEnTable : MPsTable("members_of_parliament_en")
object MPsJaTable : MPsTable("members_of_parliament_ja")
object MPsZhTable : MPsTable("members_of_parliament_zh")

fun getMPsTable(languageCode: String? = "ko"): MPsTable {
    return when (languageCode) {
        "ko" -> MPsKoTable
        "en" -> MPsEnTable
        "ja" -> MPsJaTable
        "zh" -> MPsZhTable
        else -> throw IllegalArgumentException("지원되지 않는 언어 코드: $languageCode")
    }
}