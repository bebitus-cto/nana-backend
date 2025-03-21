package com.nana.nana.domain.feature.assemblysync.mp

import com.nana.nana.domain.enums.Gender
import com.nana.nana.domain.feature.assemblysync.mp.datamodel.MPSyncDataModel
import com.nana.nana.domain.feature.translation.config.TransConfig.targetLanguages
import com.nana.nana.table.mp.getMPsTable
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.select
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class SyncTransMPRepository {

    private val logger: Logger = LoggerFactory.getLogger(SyncTransMPRepository::class.java)

    suspend fun batchUpsertMPsDetail(translatedMPs: List<Pair<String, List<Map<String, Any?>>>>) {
        translatedMPs.forEach { (targetLanguage, updates) ->

            val mpsTable = getMPsTable(targetLanguage)

            mpsTable.batchUpsert(updates) { updateMap ->
                this[mpsTable.id] = updateMap["id"] as String
                this[mpsTable.extraId] = updateMap["extraId"] as String

                updateMap["birthPlace"]?.let {
                    this[mpsTable.birthPlace] = it as String
                }
                updateMap["positionName"]?.let {
                    this[mpsTable.positionName] = it as String
                }
                updateMap["partyName"]?.let {
                    this[mpsTable.partyName] = it as String
                }
                updateMap["electoralDistrictName"]?.let {
                    this[mpsTable.electoralDistrictName] = it as String
                }
                updateMap["electoralDistrictType"]?.let {
                    this[mpsTable.electoralDistrictType] = it as String
                }
                updateMap["committeeName"]?.let {
                    this[mpsTable.committeeName] = it as String
                }
                updateMap["reElectionStatus"]?.let {
                    this[mpsTable.reElectionStatus] = it as String
                }
                updateMap["electionEraco"]?.let {
                    this[mpsTable.electionEraco] = it as String
                }
                updateMap["history"]?.let {
                    this[mpsTable.careerHistory] = it as String
                }
                updateMap["educationHistory"]?.let {
                    this[mpsTable.educationHistory] = it as String
                }
                updateMap["officeRoomNumber"]?.let {
                    this[mpsTable.officeRoomNumber] = it as String
                }

                // ✅ 번역이 필요없었던 것들
                // ✅ 1. ENUM 변경 필드 `upsert`
                updateMap["gender"]?.let {
                    this[mpsTable.gender] = it as Gender
                }

                // ✅ 2. 번역되지 않지만 파싱이 필요한 필드 `upsert`
                updateMap["birthDate"]?.let {
                    this[mpsTable.birthDate] = it as LocalDate
                }

                // ✅ 3. 번역되지 않고 파싱이 필요없지만 변경이 있을 수 있는 필드 `upsert`
                updateMap["officeEmail"]?.let {
                    this[mpsTable.officeEmail] = it as String
                }
                updateMap["officialWebsiteUrl"]?.let {
                    this[mpsTable.officialWebsiteUrl] = it as String
                }
                updateMap["profilePictureUrl"]?.let {
                    this[mpsTable.profilePictureUrl] = it as String
                }
                updateMap["officePhoneNumber"]?.let {
                    this[mpsTable.officePhoneNumber] = it as String
                }
            }
        }
    }

    suspend fun batchUpsertMPNames(finalResults: Map<String, Map<String, String?>>): Boolean {
        if (finalResults.isEmpty()) {
            logger.info("📌 Upsert 할 국회의원 다국어 이름 데이터가 없음")
            return true
        }

        // 모든 targetLanguage 키에 대해 빈 리스트를 미리 초기화
        val batchData: MutableMap<String, MutableList<Map<String, Any?>>> =
            targetLanguages.associateWith { mutableListOf<Map<String, Any?>>() }.toMutableMap()

        // 각 MP의 번역 결과를 언어별 업데이트 리스트에 추가
        for ((mpId, translations) in finalResults) {
            for (targetLanguage in targetLanguages) {
                val translatedName = translations[targetLanguage]
                if (!translatedName.isNullOrBlank()) {
                    batchData[targetLanguage]
                        ?.add(mapOf("id" to "${mpId}_$targetLanguage", "name" to translatedName))
                }
            }
        }

        val totalInputCount = batchData.values.sumOf { it.count() }
        var totalResultCount = 0

        try {
            batchData.forEach { (languageCode, updates) ->

                val translateTable = getMPsTable(languageCode)
                val resultRows = translateTable.batchUpsert(updates) { updateMap ->
                    this[translateTable.id] = updateMap["id"] as String
                    this[translateTable.name] = updateMap["name"] as String
                }
                totalResultCount += resultRows.count()
            }
            logger.info("✅ 다국어 MP 이름 batch upsert 완료! 입력 건수: $totalInputCount, 처리 건수: $totalResultCount")
            return totalInputCount == totalResultCount
        } catch (e: Exception) {
            logger.error("❌ 다국어 MP 이름 upsert 오류: ${e.message}")
            return false
        }
    }

    suspend fun selectMPsByIds(
        ids: Set<String>,
        targetLanguage: String? = null
    ): List<MPSyncDataModel> {

        val mpsTable = getMPsTable(targetLanguage)

        return mpsTable.select { mpsTable.id inList ids }
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
}