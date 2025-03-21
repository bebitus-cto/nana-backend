package com.nana.nana.domain.feature.president

import org.jetbrains.exposed.sql.Table

/**
 * 대통령 SNS 테이블 (대통령의 SNS URL 저장)
 */
object SnsPresidentTable : Table("sns_president") {

    val presidentId = varchar("president_id", 30) // 이름_생년월일 (한글 - 메인 테이블의 primary key)
    val xUrl = text("x_url").nullable()                // X (구 트위터) URL
    val facebookUrl = text("facebook_url").nullable()  // 페이스북 URL
    val youtubeUrl = text("youtube_url").nullable()    // 유튜브 URL
    val blogUrl = text("blog_url").nullable()          // 블로그 URL

    override val primaryKey = PrimaryKey(presidentId)
}