package com.nana.nana.domain.enums

enum class Nationality {
    DOMESTIC,        // 내국인
    OVERSEAS_CITIZEN, // 재외국민
    FOREIGN;          // 외국인

    companion object {
        /**
         * 검증된 국적 코드로부터 Nationality를 생성합니다.
         * @param value "L"이면 DOMESTIC, "F"이면 FOREIGN, 그 외에는 null 반환
         */
        fun fromKCBNationality(value: String?): Nationality? {
            return when (value) {
                "L" -> DOMESTIC
                "F" -> FOREIGN
                else -> null
            }
        }
    }
}