package com.nana.nana.domain.enums

/**
 * 생일 구분 코드 (양/음)
 */
enum class Birth(val krValue: String?) {
    Solar("양"),
    Lunar("음"),
    Unknown("모름");

    companion object {
        fun toBirthDivision(value: String?): Birth {
            return entries.find { it.krValue == value } ?: Unknown
        }
    }
}