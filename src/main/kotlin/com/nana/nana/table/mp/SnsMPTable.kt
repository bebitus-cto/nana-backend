package com.nana.nana.table.mp

import org.jetbrains.exposed.sql.Table

/**
 * 국회의원 SNS 테이블 (국회의원의 SNS URL 저장)
 */
object SnsMPTable : Table("sns_mp") {

    val mpId = varchar("mp_id", 30) // 이름_생년월일 (한글 - 메인 테이블의 primary key)
    val xUrl = text("x_url").nullable()                // X (구 트위터) URL
    val facebookUrl = text("facebook_url").nullable()  // 페이스북 URL
    val youtubeUrl = text("youtube_url").nullable()    // 유튜브 URL
    val blogUrl = text("blog_url").nullable()          // 블로그 URL

    override val primaryKey = PrimaryKey(mpId)
}