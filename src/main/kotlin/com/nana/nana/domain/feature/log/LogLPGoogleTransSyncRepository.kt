package com.nana.nana.domain.feature.log

import com.nana.nana.config.ApiConfig.defaultJson
import com.nana.nana.table.log.LogLPGoogleTransTable
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.sql.upsert
import org.springframework.stereotype.Repository

@Repository
class LogLPGoogleTransSyncRepository {

    suspend fun logLPGoogleTransSyncError(
        lpId: String,
        texts: List<String>,
        sourceLanguage: String,
        targetLanguage: String,
        errorMessage: String,
    ) {
        LogLPGoogleTransTable.upsert {
            it[this.lpId] = lpId
            it[this.texts] = defaultJson.encodeToString(texts)
            it[this.sourceLanguage] = sourceLanguage
            it[this.targetLanguage] = targetLanguage
            it[this.errorMessage] = errorMessage
        }
    }
}