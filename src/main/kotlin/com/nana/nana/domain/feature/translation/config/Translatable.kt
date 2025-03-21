package com.nana.nana.domain.feature.translation.config

interface Translatable {
    val koValue: String
    val enValue: String
    val zhValue: String
    val jaValue: String

    fun getTranslatedValue(targetLanguage: String): String {
        return when (targetLanguage.lowercase()) {
            "en" -> enValue
            "zh" -> zhValue
            "ja" -> jaValue
            else -> koValue
        }
    }
}