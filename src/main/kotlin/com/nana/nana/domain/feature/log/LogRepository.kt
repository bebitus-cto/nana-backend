package com.nana.nana.domain.feature.log

import com.nana.nana.config.ApiConfig.defaultJson
import com.nana.nana.domain.feature.assemblysync.lp.datamodel.LPSyncDataModel
import com.nana.nana.domain.feature.assemblysync.mp.datamodel.MPSyncDataModel
import com.nana.nana.domain.feature.translation.openai.GPTRequest
import com.nana.nana.table.log.LogGPTTransTable
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class LogRepository {

    suspend fun logGptMPBatchTransError(
        batch: List<MPSyncDataModel>,
        targetLanguages: List<String>,
        gptRequest: GPTRequest,
        errorMessage: String,
        rawResponse: String? = null,
        attemptCount: Int
    ) {
        val mpIds = batch.joinToString(separator = ",") { it.id }
        val targetLanguageStr = targetLanguages.joinToString(", ")
        val gptRequestStr = defaultJson.encodeToString(gptRequest)

        LogGPTTransTable.insert { row ->
            row[LogGPTTransTable.targetLanguages] = targetLanguageStr
            row[LogGPTTransTable.lpId] = null
            row[LogGPTTransTable.mpId] = mpIds
            row[LogGPTTransTable.gptRequest] = gptRequestStr
            row[LogGPTTransTable.errorMessage] = errorMessage
            row[LogGPTTransTable.rawResponse] = rawResponse
            row[LogGPTTransTable.attemptCount] = attemptCount
        }
    }

    suspend fun logGptLPBatchTranslationError(
        batch: List<LPSyncDataModel>,
        targetLanguages: List<String>,
        gptRequest: GPTRequest,
        errorMessage: String,
        rawResponse: String? = null,
        attemptCount: Int
    ) {
        val lpIds = batch.joinToString(separator = ",") { it.id }
        val targetLanguageStr = targetLanguages.joinToString(", ")
        val gptRequestStr = defaultJson.encodeToString(gptRequest)

        LogGPTTransTable.insert { row ->
            row[LogGPTTransTable.targetLanguages] = targetLanguageStr
            row[LogGPTTransTable.lpId] = lpIds
            row[LogGPTTransTable.mpId] = null
            row[LogGPTTransTable.gptRequest] = gptRequestStr
            row[LogGPTTransTable.errorMessage] = errorMessage
            row[LogGPTTransTable.rawResponse] = rawResponse
            row[LogGPTTransTable.attemptCount] = attemptCount
        }
    }

    fun getFailedMPNameTransIds(): Map<Int, Set<String>> {
        return LogGPTTransTable
            .selectAll()
            .associate { row ->
                val rowId = row[LogGPTTransTable.id]
                val mpIdsStr = row[LogGPTTransTable.mpId].orEmpty()
                val mpIdSet = mpIdsStr
                    .split(",")
                    .filter { it.isNotEmpty() }
                    .toSet()

                rowId to mpIdSet
            }
    }

    fun markFailedMpNamesAsRetried(ids: List<Int>, isSuccess: Boolean) {
        LogGPTTransTable
            .update({ LogGPTTransTable.id inList ids }) {
                it[resolved] = isSuccess
                it[resolvedAt] = LocalDateTime.now()
                with(SqlExpressionBuilder) {
                    it[retryCount] = retryCount + 1
                }
            }
    }
}