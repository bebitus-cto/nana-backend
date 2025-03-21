package com.nana.nana.domain.feature.assemblysync.mp

import com.nana.nana.domain.feature.assemblysync.config.SyncConfig.CURRENT_MP_ERACO
import com.nana.nana.domain.feature.assemblysync.mp.apiclient.json.SyncMPSnsJsonProperty
import com.nana.nana.domain.feature.assemblysync.mp.datamodel.MPSyncDataModel
import com.nana.nana.domain.feature.assemblysync.sns.SyncSnsRepository
import com.nana.nana.table.mp.getMPsTable
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class SyncMPRepository(
    private val syncSnsRepository: SyncSnsRepository
) {

    private val logger: Logger = LoggerFactory.getLogger(SyncMPRepository::class.java)

    suspend fun batchUpsertMPsDetail(
        newKeys: Set<String>,
        dataModels: List<MPSyncDataModel>,
        snsMap: Map<String, SyncMPSnsJsonProperty>
    ): List<MPSyncDataModel> {

        val mpsTable = getMPsTable()

        val resultRows = mpsTable.batchUpsert(dataModels) { mp ->
            this[mpsTable.id] = mp.id
            this[mpsTable.extraId] = mp.extraId
            this[mpsTable.name] = mp.name
            this[mpsTable.birthDate] = mp.birthDate
            this[mpsTable.birthPlace] = null // 2차 처리 예정
            this[mpsTable.gender] = mp.gender
            this[mpsTable.positionName] = mp.positionName
            this[mpsTable.partyName] = mp.partyName
            this[mpsTable.electoralDistrictName] = mp.electoralDistrictName
            this[mpsTable.electoralDistrictType] = mp.electoralDistrictType
            this[mpsTable.committeeName] = mp.committeeName
            this[mpsTable.reElectionStatus] = mp.reElectionStatus
            this[mpsTable.electionEraco] = mp.electionEraco
            this[mpsTable.officePhoneNumber] = mp.officePhoneNumber
            this[mpsTable.officeEmail] = mp.officeEmailAddress
            this[mpsTable.officialWebsiteUrl] = mp.officialWebsiteUrl
            this[mpsTable.careerHistory] = null // 2차 처리 예정
            this[mpsTable.educationHistory] = null // 2차 처리 예정
            this[mpsTable.officeRoomNumber] = mp.officeRoomNumber
            this[mpsTable.profilePictureUrl] = mp.profilePictureUrl
        }

        val inputCount = dataModels.count()
        val insertedCount = resultRows.count()

        val dbDataMap = selectMPsByIdsMap(newKeys)
        val dbData = dbDataMap.values.toList()
        val dbKeys = dbDataMap.keys
        val failedKeys = newKeys - dbKeys
        val failedData = dataModels.filter { mp ->
            val key = mp.id
            key in failedKeys
        }

        syncSnsRepository.batchUpsertSns(dbData, snsMap)

        if (insertedCount == inputCount) {
            logger.warn("국회의원 데이터가 배치처리 성공: ${dbData.count()}개")
            return dbData
        } else {
            logger.warn("일부 국회의원 데이터가 배치처리 중 Upsert 실패: ${failedData.map { it.name }}")
            return failedData.toList()
        }
    }

    suspend fun selectMPsByIds(ids: Set<String>): List<MPSyncDataModel> {

        val mpsTable = getMPsTable()

        return mpsTable
            .select { mpsTable.id inList ids }
            .map { row ->
                MPSyncDataModel(
                    id = row[mpsTable.id],
                    extraId = row[mpsTable.extraId],
                    name = row[mpsTable.name].orEmpty(),
                    birthDate = row[mpsTable.birthDate],
                    positionName = row[mpsTable.positionName],
                    partyName = row[mpsTable.partyName],
                    electoralDistrictName = row[mpsTable.electoralDistrictName],
                    electoralDistrictType = row[mpsTable.electoralDistrictType],
                    committeeName = row[mpsTable.committeeName],
                    reElectionStatus = row[mpsTable.reElectionStatus],
                    electionEraco = row[mpsTable.electionEraco],
                    gender = row[mpsTable.gender],
                    officePhoneNumber = row[mpsTable.officePhoneNumber],
                    officeEmailAddress = row[mpsTable.officeEmail],
                    officialWebsiteUrl = row[mpsTable.officialWebsiteUrl],
                    history = null,
                    officeRoomNumber = row[mpsTable.officeRoomNumber],
                    profilePictureUrl = row[mpsTable.profilePictureUrl]
                )
            }
    }

    suspend fun selectMPsByIdsMap(ids: Set<String>): Map<String, MPSyncDataModel> {
        return selectMPsByIds(ids).associateBy { it.id }
    }

    suspend fun selectAllMPsByNameMap(eraco: Int = CURRENT_MP_ERACO): Map<String, MPSyncDataModel> {
        val mpsTable = getMPsTable()
        return mpsTable
            .select { mpsTable.electionEraco like "%${eraco}%" }
            .map { row ->
                MPSyncDataModel(
                    id = row[mpsTable.id],
                    extraId = row[mpsTable.extraId],
                    name = row[mpsTable.name].orEmpty(),
                    birthDate = row[mpsTable.birthDate],
                    positionName = row[mpsTable.positionName],
                    partyName = row[mpsTable.partyName],
                    electoralDistrictName = row[mpsTable.electoralDistrictName],
                    electoralDistrictType = row[mpsTable.electoralDistrictType],
                    committeeName = row[mpsTable.committeeName],
                    reElectionStatus = row[mpsTable.reElectionStatus],
                    electionEraco = row[mpsTable.electionEraco],
                    gender = row[mpsTable.gender],
                    officePhoneNumber = row[mpsTable.officePhoneNumber],
                    officeEmailAddress = row[mpsTable.officeEmail],
                    officialWebsiteUrl = row[mpsTable.officialWebsiteUrl],
                    history = null,
                    officeRoomNumber = row[mpsTable.officeRoomNumber],
                    profilePictureUrl = row[mpsTable.profilePictureUrl]
                )
            }.associateBy { it.name }
    }


}