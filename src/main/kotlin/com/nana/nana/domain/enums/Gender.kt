package com.nana.nana.domain.enums

/**
 * 성별 (남/여)
 */
enum class Gender(val krValue: String?) {
    MALE("남"),
    FEMALE("여");

    companion object {

        fun toGender(value: String?): Gender? = entries.find { it.krValue == value }
        fun fromKCBGender(verifiedSexCode: String?): Gender? =
            if (verifiedSexCode == null) {
                null
            } else {
                when (verifiedSexCode) {
                    "M" -> MALE
                    "F" -> FEMALE
                    else -> null
                }
            }
    }
}