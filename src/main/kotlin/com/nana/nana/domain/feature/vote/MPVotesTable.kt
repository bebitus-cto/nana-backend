package com.nana.nana.domain.feature.vote

import org.jetbrains.exposed.sql.Table

object MPVotesTable : Table("mp_votes") {

    val mpId = varchar("mp_id", 30) // 국회의원 ID (PK)
    val goodVote = integer("good_vote").default(0) // 찬성 수
    val badVote = integer("bad_vote").default(0) // 반대 수

    override val primaryKey = PrimaryKey(mpId)
}