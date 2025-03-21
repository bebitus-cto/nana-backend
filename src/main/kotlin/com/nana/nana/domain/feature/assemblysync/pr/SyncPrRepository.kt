package com.nana.nana.domain.feature.assemblysync.pr

import com.nana.nana.domain.enums.ElectionStatus
import com.nana.nana.domain.enums.Party
import com.nana.nana.domain.feature.assemblysync.pr.datamodel.PrSyncDataModel
import com.nana.nana.table.pr.getPrsTable
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.select
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class SyncPrRepository {

    private val logger: Logger = LoggerFactory.getLogger(SyncPrRepository::class.java)

    suspend fun batchUpsertPrsDetail(dataModels: List<PrSyncDataModel>): List<PrSyncDataModel> {

        val newKeys = dataModels.map { it.id }.toSet()

        val prsTable = getPrsTable()

        val resultRows = prsTable.batchUpsert(dataModels) { pr ->
            this[prsTable.id] = pr.id
            this[prsTable.name] = pr.name
            this[prsTable.birthDate] = pr.birthDate
            this[prsTable.termStartDate] = pr.termStartDate
            this[prsTable.termEndDate] = pr.termEndDate
            this[prsTable.gender] = pr.gender
            this[prsTable.positionName] = pr.positionName
            this[prsTable.partyName] = pr.partyName
            this[prsTable.electoralDistrictName] = pr.electoralDistrictName
            this[prsTable.electoralDistrictType] = pr.electoralDistrictDivisionName
            this[prsTable.committeeName] = pr.committeeName
            this[prsTable.reElectionStatus] = pr.reElectionStatus
            this[prsTable.electionEraco] = pr.electionEraco
            this[prsTable.officePhoneNumber] = pr.officePhoneNumber
            this[prsTable.officeEmailAddress] = pr.officeEmailAddress
            this[prsTable.officialWebsiteUrl] = pr.officialWebsiteUrl
            this[prsTable.careerHistory] = null /*pr.history*/
            this[prsTable.officeRoomNumber] = pr.officeRoomNumber
            this[prsTable.profilePictureUrl] = pr.profilePictureUrl
        }

        val inputCount = dataModels.count()
        val insertedCount = resultRows.count()

        val dbData = selectPrsByIds(newKeys)
        val dbKeys = dbData.map { it.id }.toSet()
        val failedKeys = newKeys - dbKeys
        val failedData = dataModels.filter { mp ->
            val key = mp.id
            key in failedKeys
        }

        if (insertedCount == inputCount) {
            logger.warn("대통령 데이터가 배치처리 성공: ${dbData.count()}개")
            return dbData
        } else {
            logger.warn("일부 대통령 데이터가 배치처리 중 Upsert 실패: ${failedData.map { it.name }}")
            return failedData.toList()
        }
    }

    suspend fun selectPrsByIds(ids: Set<String>): List<PrSyncDataModel> {

        val prsTable = getPrsTable()

        return prsTable.select { prsTable.id inList ids }
            .map { row ->
                PrSyncDataModel(
                    id = row[prsTable.id],
                    name = row[prsTable.name],
                    termStartDate = row[prsTable.termStartDate],
                    termEndDate = row[prsTable.termEndDate],
                    birthDate = row[prsTable.birthDate],
                    partyName = row[prsTable.partyName],
                    electoralDistrictName = row[prsTable.electoralDistrictName],
                    electoralDistrictDivisionName = row[prsTable.electoralDistrictType],
                    committeeName = row[prsTable.committeeName],
                    reElectionStatus = row[prsTable.reElectionStatus],
                    electionEraco = row[prsTable.electionEraco],
                    gender = row[prsTable.gender],
                    officePhoneNumber = row[prsTable.officePhoneNumber],
                    officeEmailAddress = row[prsTable.officeEmailAddress],
                    officialWebsiteUrl = row[prsTable.officialWebsiteUrl],
                    history = null,
                    officeRoomNumber = row[prsTable.officeRoomNumber],
                    profilePictureUrl = row[prsTable.profilePictureUrl],
                )
            }
            .toList()
    }

    suspend fun batchUpsertTransPrsDetailMultiLang(dataModels: List<PrSyncDataModel>): List<PrSyncDataModel> {

        // 2. 대상 언어(en, zh, ja)에 대해 번역 후 업설트 (한국어("ko")는 건너뜀)
        for (targetLanguage in targetLanguages) {

            // 각 데이터 모델에 대해, 대상 언어에 맞게 번역된 필드 적용
            val translatedDataModels = dataModels.map { pr ->
                // 예시: reElectionStatus 번역
                val translatedReElectionStatus =
                    ElectionStatus.lookupByKoValue[pr.reElectionStatus]
                        ?.getTranslatedValue(targetLanguage) ?: pr.reElectionStatus

                // 예시: partyName의 "/"로 분리된 각 토큰 번역
                val translatedPartyNames =
                    pr.partyName?.split("/")?.map { token ->
                        Party.lookupByKoValue[token.trim()]?.getTranslatedValue(targetLanguage) ?: token.trim()
                    }?.joinToString(separator = "/") ?: pr.partyName

                pr.copy(
                    reElectionStatus = translatedReElectionStatus,
                    partyName = translatedPartyNames
                )
            }

            // 대상 언어에 해당하는 테이블 얻기
            val langTable = getPrsTable()
            val langResultRows = langTable.batchUpsert(translatedDataModels) { pr ->
                this[langTable.id] = pr.id
                this[langTable.gender] = pr.gender
            }
            logger.info("✅ ${targetLanguage} 대통령 데이터 배치 업설트 완료: ${langResultRows.count()}건")
        }

        return emptyList()
    }

}