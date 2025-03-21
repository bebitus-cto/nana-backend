package com.nana.nana.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * 날짜 파싱
 */
object DateParser {

    private val logger: Logger = LoggerFactory.getLogger(DateParser::class.java)

    fun toLocalDate(value: String?): LocalDate? {
        if (value == null) {
            return null
        }

        // 월 또는 일이 "00"인 경우 날짜 값이 없음으로 처리
        if (Regex("""\d{4}-00-\d{2}""").matches(value) ||
            Regex("""\d{4}-\d{2}-00""").matches(value) ||
            Regex("""\d{4}-00-00""").matches(value)
        ) {
            logger.info("날짜 값이 없음: 입력값='$value'")
            return null
        }

        return try {
            LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: DateTimeParseException) {
            when {
                value.endsWith("-02-30") || value.endsWith("-02-31") -> {
                    logger.error("비정상적인 날짜(잘못된 일자): 입력값='$value'")
                    null
                }

                value.endsWith("-02-29") -> {
                    val year = value.substring(0, 4).toIntOrNull() ?: return null
                    if (!isLeapYear(year)) {
                        logger.error("잘못된 윤년 입력: 입력값='$value'")
                    }
                    null
                }

                else -> {
                    logger.error("날짜 파싱 오류: 입력값='$value', 에러=${e.message}")
                    null
                }
            }
        }
    }

    fun parseDateStr(date: LocalDate?): String? {
        return date?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    /**
     * 생년월일 문자열에서 YYYY-MM-DD 형식의 부분만 추출합니다.
     * 예를 들어 "  1914     "나 "1914-  -  "와 같이 부정확한 문자열이 들어와도,
     * 정규 표현식으로 올바른 날짜 형식만 추출하려고 시도합니다.
     */
    fun normalizeBirthDate(dateStr: String?): String? {
        if (dateStr == null) return null
        // 좌우 공백 제거
        val trimmed = dateStr.trim()
        // YYYY-MM-DD 패턴 정규 표현식 (예: "1899-05-07")
        val regex = Regex("""\d{4}-\d{2}-\d{2}""")
        return regex.find(trimmed)?.value
    }

    /**
     * 입력된 문자열에서 4자리 연도만 추출합니다.
     */
    fun extractYear(dateStr: String?): String? {
        if (dateStr == null) return null
        val trimmed = dateStr.trim()
        val regex = Regex("""\d{4}""")
        return regex.find(trimmed)?.value
    }

    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }
}