package com.nana.nana.domain.feature.auth.google

import com.nana.nana.domain.enums.Gender
import com.nana.nana.domain.enums.JoinMethod
import com.nana.nana.domain.enums.Nationality
import com.nana.nana.domain.enums.UserStatus
import com.nana.nana.domain.feature.auth.user.UserDataModel
import com.nana.nana.table.user.UsersTable
import com.nana.nana.util.DateParser
import org.jetbrains.exposed.sql.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class AuthUserRepository {

    private val logger = LoggerFactory.getLogger(AuthUserRepository::class.java)

    suspend fun findByEmail(email: String): UserDataModel? {

        val usersTable = UsersTable

        return usersTable
            .select { usersTable.email eq email }
            .map { row ->
                UserDataModel(
                    id = row[usersTable.id],
                    email = row[usersTable.email],
                    name = row[usersTable.name],
                    status = row[usersTable.status],
                    isIdentityVerified = row[usersTable.isIdentityVerified],
                    phoneNumber = row[usersTable.phoneNumber],
                    birthDate = row[usersTable.birthDate],
                    gender = row[usersTable.gender],
                    joinMethod = row[usersTable.joinMethod],
                    joinTime = row[usersTable.joinTime]
                )
            }.singleOrNull()
    }

    suspend fun findById(userId: Long): UserDataModel? {
        return UsersTable.select { UsersTable.id eq userId }
            .map { row ->
                UserDataModel(
                    id = row[UsersTable.id],
                    email = row[UsersTable.email],
                    name = row[UsersTable.name],
                    status = row[UsersTable.status],
                    isIdentityVerified = row[UsersTable.isIdentityVerified],
                    phoneNumber = row[UsersTable.phoneNumber],
                    birthDate = row[UsersTable.birthDate],
                    gender = row[UsersTable.gender],
                    joinMethod = row[UsersTable.joinMethod],
                    joinTime = row[UsersTable.joinTime]
                )
            }.singleOrNull()
    }

    suspend fun createUser(
        email: String?,
        name: String?,
        profilePictureUrl: String?,
        joinMethod: JoinMethod
    ): UserDataModel {
        val usersTable = UsersTable

        val result = usersTable.insert { row ->
            row[usersTable.email] = email.orEmpty()
            row[usersTable.name] = name.orEmpty()
            row[usersTable.pictureUrl] = profilePictureUrl
            row[usersTable.status] = UserStatus.ACTIVE
            row[usersTable.isIdentityVerified] = false
            row[usersTable.joinMethod] = joinMethod
            row[usersTable.joinTime] = LocalDateTime.now()
        }

        return UserDataModel(
            id = result[UsersTable.id],
            email = result[UsersTable.email],
            name = result[UsersTable.name],
            status = result[UsersTable.status],
            isIdentityVerified = result[UsersTable.isIdentityVerified],
            phoneNumber = result[UsersTable.phoneNumber],
            birthDate = result[UsersTable.birthDate],
            gender = result[UsersTable.gender],
            joinMethod = result[UsersTable.joinMethod],
            joinTime = result[UsersTable.joinTime]
        )
    }

    suspend fun saveRefreshToken(userId: Long, refreshToken: String) {
        val usersTable = UsersTable

        usersTable
            .update({ usersTable.id eq userId })
            { row ->
                row[usersTable.refreshToken] = refreshToken
            }
    }

    suspend fun updateKCBUserInfo(
        email: String,
        kcbName: String,
        kcbBirthDate: String?,
        kcbSexCode: String?,
        kcbNationality: String?,
        kcbPhoneNumber: String?
    ): UserDataModel {
        val usersTable = UsersTable

        usersTable.update(
            { usersTable.email eq email }) { row ->

            row[isIdentityVerified] = true
            row[name] = kcbName
            row[birthDate] = DateParser.toLocalDate(kcbBirthDate)
            row[gender] = Gender.fromKCBGender(kcbSexCode)
            row[nationality] = Nationality.fromKCBNationality(kcbNationality)
            row[phoneNumber] = kcbPhoneNumber
        }
        return findByEmail(email) ?: throw IllegalStateException("업데이트 후 사용자 정보를 조회할 수 없음")
    }
}