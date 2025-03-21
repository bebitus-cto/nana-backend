package com.nana.nana.domain.feature.log

import com.nana.nana.table.log.LogLPSyncTable
import org.jetbrains.exposed.sql.upsert
import org.springframework.stereotype.Repository

@Repository
class LogLPSyncRepository {

    suspend fun logLPSyncError(message: String, id: String) {
        LogLPSyncTable.upsert {
            it[lpId] = id
            it[errorMessage] = message
        }
    }
}