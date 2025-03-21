package com.nana.nana.table.user

import com.nana.nana.domain.enums.Gender
import com.nana.nana.domain.enums.JoinMethod
import com.nana.nana.domain.enums.Nationality
import com.nana.nana.domain.enums.UserStatus
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime

object UsersTable : Table("users") {
    val id = long("id").autoIncrement()             // 유저 고유 아이디 (PK)
    val name = varchar("name", 255)          // 유저이름
    val pictureUrl = varchar("picture_url", 255).nullable()  // 유저프로필 (URL 등)
    val nickname = varchar("nickname", 255).nullable()  // 유저별명
    val email = varchar("email", 255).uniqueIndex() // 유저아이디 (보통 이메일을 사용)
    val joinTime = datetime("join_time")            // 유저가입시간
    val joinMethod = enumerationByName("join_method", 20, JoinMethod::class)  // 유저가입수단
    val status = enumerationByName("status", 20, UserStatus::class)  // 유저상태
    val isIdentityVerified = bool("is_identity_verified") // 유저 본인인증 여부
    val nationality = enumerationByName("nationality", 20, Nationality::class).nullable()            // 내국인(true) / 외국인(false)
    val phoneNumber = varchar("phone_number", 20).nullable()  // 폰넘버
    val birthDate = date("birth_date").nullable()     // 유저생일
    val gender = enumerationByName("gender", 20, Gender::class).nullable()  // 유저상태
    val refreshToken = varchar("refresh_token", 255)

    override val primaryKey = PrimaryKey(id)
}