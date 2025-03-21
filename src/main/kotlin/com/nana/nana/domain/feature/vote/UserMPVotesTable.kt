package com.nana.nana.domain.feature.vote

import com.nana.nana.domain.enums.VoteType
import com.nana.nana.domain.feature.vote.UserLawProposalVotesTable.default
import com.nana.nana.domain.feature.vote.UserLawProposalVotesTable.defaultExpression
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object UserMPVotesTable : Table("user_mp_votes") {

    val userId = varchar("user_id", 50) // 유저 ID
    val mpId = varchar("mp_id", 50) // 국회의원 ID
    val voteType = enumerationByName("vote_type", 10, VoteType::class) // 찬성(GOOD) / 반대(BAD)
    val voteLocked = bool("vote_locked").default(false) // ✅ 입법예고 이후 변경 불가 여부
    val lastVotedAt = timestamp("last_voted_at").defaultExpression(CurrentTimestamp()) // ✅ 24시간 제한을 위한 마지막 투표 시간
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp()) // 투표 등록 시각

    override val primaryKey = PrimaryKey(userId, mpId)
}