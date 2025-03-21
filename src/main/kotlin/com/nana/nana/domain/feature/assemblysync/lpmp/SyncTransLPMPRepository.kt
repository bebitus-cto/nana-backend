package com.nana.nana.domain.feature.assemblysync.lpmp

import com.nana.nana.domain.feature.assemblysync.mp.datamodel.MPSyncDataModel
import com.nana.nana.table.mp.getMPsTable
import org.jetbrains.exposed.sql.select
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class SyncTransLPMPRepository {

    private val logger: Logger = LoggerFactory.getLogger(SyncTransLPMPRepository::class.java)

    suspend fun mapAndSelectTranslatedMPNames(
        existingMPs: Map<String, MPSyncDataModel>,
        mpNames: List<String>,
        targetLanguage: String
    ): List<String> {

        if (mpNames.isEmpty()) {
            return emptyList()
        }

        val translateTable = getMPsTable(targetLanguage)

        val ids = mpNames.mapNotNull { name ->
            existingMPs[name]?.let { it.id + "_$targetLanguage" }
        }

        if (ids.isEmpty()) {
            return emptyList()
        }

        val translatedMap = translateTable
            .select { translateTable.id inList ids }
            .associate { it[translateTable.id].removeSuffix("_$targetLanguage") to it[translateTable.name] }

        return mpNames.map { name ->
            val translatedName = existingMPs[name]?.let { translatedMap[it.id] } ?: name
            if (translatedName == name) {
                logger.warn("번역된 이름을 찾을 수 없음: $name ($targetLanguage)")
            }
            translatedName
        }
    }
}