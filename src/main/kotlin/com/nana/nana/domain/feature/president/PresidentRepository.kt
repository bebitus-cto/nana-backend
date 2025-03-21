package com.nana.nana.domain.feature.president

import com.nana.nana.domain.enums.Birth
import com.nana.nana.domain.enums.Gender
import com.nana.nana.domain.feature.president.response.PresidentResponse
import com.nana.nana.util.DateParser
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class PresidentRepository {

    private val logger: Logger = LoggerFactory.getLogger(PresidentRepository::class.java)

    fun insertPresidentDetails(fetchPresidents: List<PresidentResponse>) {
        transaction {
            fetchPresidents.forEach { president ->
                insertPresidentDetail(president)
            }
        }
    }

    private fun insertPresidentDetail(president: PresidentResponse) {
        PresidentsTable.insertIgnore {
            it[id] = "${president.name}_${DateParser.toLocalDate(president.birthDate)}"
            it[name] = president.name
            it[nameInChinese] = president.nameInChinese
            it[nameInEnglish] = president.nameInEnglish
            it[birthDivision] = Birth.toBirthDivision(president.birthDateDivision)
            it[birthDate] = DateParser.toLocalDate(president.birthDate)
            it[gender] = Gender.toGender(president.gender)
            it[positionName] = president.positionName
            it[partyName] = president.partyName
            it[electoralDistrictName] = president.electoralDistrictName
            it[electoralDistrictDivisionName] = president.electoralDistrictDivisionName
            it[committeeName] = president.committeeName
            it[reElectionDivisionName] = president.reElectionDivisionName
            it[electionEraco] = president.electionEraco
            it[officePhoneNumber] = president.officePhoneNumber
            it[officeEmail] = president.officeEmailAddress
            it[officialWebsiteUrl] = president.officialWebsiteUrl
            it[aideName] = president.aideName
            it[chiefSecretaryName] = president.chiefSecretaryName
            it[secretaryName] = president.secretaryName
            it[careerHistory] = president.careerHistory
            it[officeRoomNumber] = president.officeRoomNumber
            it[profilePictureUrl] = president.profilePictureUrl
            it[termStartDate] = DateParser.toLocalDate(president.termStartDate)
            it[termEndDate] = DateParser.toLocalDate(president.termEndDate)
        }

        logger.info("삽입 완료: 대통령 이름=${president.name}, 생년월일=${president.birthDate}, 정당=${president.partyName}")
    }
}