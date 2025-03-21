package com.nana.nana.table.log

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object LogLPSyncTable : Table("log_lp_sync") {

    val id = integer("id").autoIncrement()
    val lpId = text("lp_id").nullable()
    val errorMessage = text("error_message").nullable()
    val successMessage = text("success_message").nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val resolvedAt = datetime("resolved_at").nullable() // 해결된 시간, 아직 해결되지 않았으면 null
    val method = text("method").nullable() // 발생 위치

    override val primaryKey = PrimaryKey(id)
}