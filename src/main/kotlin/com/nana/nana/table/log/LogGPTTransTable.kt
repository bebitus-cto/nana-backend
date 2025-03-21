package com.nana.nana.table.log

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object LogGPTTransTable : Table("log_gpt_trans") {

    val id = integer("id").autoIncrement()
    val mpId = text("mp_id").nullable()
    val lpId = text("lp_id").nullable()
    val targetLanguages = text("target_languages")
    val gptRequest = text("gpt_request")
    val errorMessage = text("error_message")
    val rawResponse = text("raw_response").nullable()
    val resolved = bool("resolved").default(false) // 해결 여부
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val attemptCount = integer("attempt_count").default(1)
    val resolvedAt = datetime("resolved_at").nullable() // 해결된 시간, 아직 해결되지 않았으면 null
    val retryCount = integer("retry_count").default(0) // 해결 여부

    override val primaryKey = PrimaryKey(id)
}