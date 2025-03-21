package com.nana.nana.domain.feature.auth.kcb

import com.nana.nana.domain.feature.auth.google.AuthUserRepository
import com.nana.nana.domain.feature.auth.user.UserDataModel
import org.springframework.stereotype.Service

@Service
class KcbService(
    private val repository: AuthUserRepository
) {

    suspend fun updateKCBUserInfo(
        email: String,
        kcbName: String,
        kcbBirthDate: String?,
        kcbSexCode: String?,
        kcbNationality: String?,
        kcbPhoneNumber: String?
    ): UserDataModel =
        repository.updateKCBUserInfo(
            email,
            kcbName,
            kcbBirthDate,
            kcbSexCode,
            kcbNationality,
            kcbPhoneNumber
        )
}