package com.nana.nana.domain.enums

import com.nana.nana.domain.feature.translation.config.Translatable
import kotlinx.serialization.Serializable

@Serializable
enum class ProportionalMPStatus(
    override val koValue: String,
    override val enValue: String,
    override val zhValue: String,
    override val jaValue: String
) : Translatable {
    ELECTED(
        "당선",
        "Elected",
        "当选",
        "当選"
    ),
    DEFEATED(
        "낙선",
        "Defeated",
        "落选",
        "落選"
    ),
    INVALID_REGISTRATION(
        "등록무효",
        "Invalid Registration",
        "注册无效",
        "登録無効"
    ),
    RESIGNED(
        "사퇴",
        "Resigned",
        "辞职",
        "辞任"
    )
}