package com.nana.nana.table.log

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object LogLPGoogleTransTable : Table("log_google_trans_sync") {

    val id = integer("id").autoIncrement()
    val lpId = text("lp_id").nullable()
    val texts = text("texts").nullable()
    val sourceLanguage = text("source_language").nullable()
    val targetLanguage = text("target_language").nullable()
    val errorMessage = text("error_message").nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }

    override val primaryKey = PrimaryKey(id)
}