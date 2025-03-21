package com.nana.nana.table.proportional

import com.nana.nana.domain.enums.ProportionalMPStatus
import org.jetbrains.exposed.sql.Table

object ProportionalReqMPsTable : Table("proportional_req_mps") {

    val id = integer("id").autoIncrement()
    val electionNumber = integer("election_number")
    val name = varchar("name", 255)
    val partyName = varchar("party_name", 255)
    val proportionalPartyName = varchar("proportional_party_name", 255).nullable()
    val proportionalRepOrder = integer("proportional_rep_order").nullable() // 비례대표 번호
    val statusStr = varchar("status_str", 50).nullable()
    val status = enumerationByName("status", 20, ProportionalMPStatus::class).nullable() // 당선, 낙선

    override val primaryKey = PrimaryKey(id)
}
