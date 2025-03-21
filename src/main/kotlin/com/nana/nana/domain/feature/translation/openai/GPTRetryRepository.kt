package com.nana.nana.domain.feature.translation.openai

import com.nana.nana.domain.feature.assemblysync.mp.datamodel.MPSyncDataModel
import com.nana.nana.table.mp.MPsKoTable
import com.nana.nana.table.mp.MPsTable
import com.nana.nana.table.mp.getMPsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class GPTRetryRepository {

    private val logger = LoggerFactory.getLogger(GPTRetryRepository::class.java)

    fun selectMPsWithMissingMultilingualName(
        ids: Set<String>
    ): List<MPSyncDataModel> {
        val ko = MPsKoTable
        // 대상 언어 테이블을 targetLanguages로부터 가져옵니다.
        val languageTables: Map<String, MPsTable> = targetLanguages.associateWith { lang ->
            getMPsTable(lang)
        }

        // 초기 LEFT JOIN: 첫 번째 대상 언어 테이블과 JOIN
        val firstLang = targetLanguages.first()
        var joined: Join = ko.leftJoin(
            languageTables[firstLang]!!,
            { ko.id },
            { languageTables[firstLang]!!.id },
            additionalConstraint = {
                languageTables[firstLang]!!.id eq CustomFunction(
                    "CONCAT",
                    TextColumnType(),
                    ko.id,
                    stringLiteral("_$firstLang")
                )
            }
        )

        // 나머지 대상 언어에 대해 LEFT JOIN 추가
        targetLanguages.drop(1).forEach { lang ->
            val table = languageTables[lang]!!
            joined = joined.leftJoin(
                table,
                { ko.id },
                { table.id },
                additionalConstraint = {
                    table.id eq CustomFunction("CONCAT", TextColumnType(), ko.id, stringLiteral("_$lang"))
                }
            )
        }

        // 각 대상 언어 테이블의 name 컬럼이 null이거나 빈 문자열("")인 조건들을 OR로 결합합니다.
        val missingCondition = languageTables.values
            .map { table -> (table.name.isNull() or (table.name eq "")) }
            .reduce { acc, op -> acc or op }

        // 조건에 맞는 MP를 조회합니다.
        return joined.select {
            (ko.id inList ids) and missingCondition
        }.map { row ->
            MPSyncDataModel(
                id = row[ko.id],
                extraId = row[ko.extraId],
                name = row[ko.name].orEmpty(),
                birthDate = row[ko.birthDate],
                positionName = row[ko.positionName],
                partyName = row[ko.partyName],
                electoralDistrictName = row[ko.electoralDistrictName],
                electoralDistrictType = row[ko.electoralDistrictType],
                committeeName = row[ko.committeeName],
                reElectionStatus = row[ko.reElectionStatus],
                electionEraco = row[ko.electionEraco],
                gender = row[ko.gender],
                officePhoneNumber = row[ko.officePhoneNumber],
                officeEmailAddress = row[ko.officeEmail],
                officialWebsiteUrl = row[ko.officialWebsiteUrl],
                history = null,
                officeRoomNumber = row[ko.officeRoomNumber],
                profilePictureUrl = row[ko.profilePictureUrl]
            )
        }
    }
}