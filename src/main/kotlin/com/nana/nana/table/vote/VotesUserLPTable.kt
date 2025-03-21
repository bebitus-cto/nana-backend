package com.nana.nana.table.vote

import com.nana.nana.domain.enums.VoteType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object VotesUserLPTable : Table("user_law_proposal_votes") {

    val userId = varchar("user_id", 50) // 유저 ID
    val lawProposalId = varchar("law_proposal_id", 30) // 의안 번호
    val voteType = enumerationByName("vote_type", 10, VoteType::class) // 찬성(GOOD) / 반대(BAD)
    val voteLocked = bool("vote_locked").default(false) // ✅ 입법예고 이후 변경 불가 여부
    val lastVotedAt = timestamp("last_voted_at").defaultExpression(CurrentTimestamp()) // ✅ 24시간 제한을 위한 마지막 투표 시간
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp()) // 투표 등록 시각
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp()) // 마지막 변경 시각

    override val primaryKey = PrimaryKey(userId, lawProposalId)
}