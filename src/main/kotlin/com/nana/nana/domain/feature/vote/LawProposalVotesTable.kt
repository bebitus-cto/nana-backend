package com.nana.nana.domain.feature.vote

import org.jetbrains.exposed.sql.Table

object LawProposalVotesTable : Table("law_proposal_votes") {

    val lawProposalId = varchar("law_proposal_id", 30) // 의안 번호 (PK)
    val goodVote = integer("good_vote").default(0) // 찬성 수
    val badVote = integer("bad_vote").default(0) // 반대 수

    override val primaryKey = PrimaryKey(lawProposalId)
}