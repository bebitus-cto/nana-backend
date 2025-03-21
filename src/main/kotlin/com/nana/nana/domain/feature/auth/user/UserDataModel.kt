package com.nana.nana.domain.feature.auth.user

import com.nana.nana.domain.enums.Gender
import com.nana.nana.domain.enums.JoinMethod
import com.nana.nana.domain.enums.Nationality
import com.nana.nana.domain.enums.UserStatus
import java.time.LocalDate
import java.time.LocalDateTime

data class UserDataModel(
    val id: Long,
    val name: String,
    val profilePictureUrl: String? = null,
    val nickname: String? = null,
    val email: String? = null,
    val joinTime: LocalDateTime,
    val joinMethod: JoinMethod,
    val status: UserStatus,
    val isIdentityVerified: Boolean,
    val nationality: Nationality? = null,
    val phoneNumber: String? = null,
    val birthDate: LocalDate? = null,
    val gender: Gender? = null
)