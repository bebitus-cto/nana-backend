package com.nana.nana.domain.enums

import com.nana.nana.domain.feature.translation.config.Translatable

enum class ElectionStatus(
    override val koValue: String,
    override val enValue: String,
    override val zhValue: String,
    override val jaValue: String
) : Translatable {
    FIRST_TERM(
        koValue = "초선",
        enValue = "First Term",
        zhValue = "首任",
        jaValue = "初当選"
    ),
    SECOND_TERM(
        koValue = "재선",
        enValue = "Second Term",
        zhValue = "连任",
        jaValue = "再選"
    ),
    THIRD_TERM(
        koValue = "3선",
        enValue = "Third Term",
        zhValue = "第三任",
        jaValue = "第三任期"
    ),
    FOURTH_TERM(
        koValue = "4선",
        enValue = "Fourth Term",
        zhValue = "第四任",
        jaValue = "第四任期"
    ),
    FIFTH_TERM(
        koValue = "5선",
        enValue = "Fifth Term",
        zhValue = "第五任",
        jaValue = "第五任期"
    ),
    SIXTH_TERM(
        koValue = "6선",
        enValue = "Sixth Term",
        zhValue = "第六任",
        jaValue = "第六任期"
    ),
    SEVENTH_TERM(
        koValue = "7선",
        enValue = "Seventh Term",
        zhValue = "第七任",
        jaValue = "第七任期"
    ),
    EIGHTH_TERM(
        koValue = "8선",
        enValue = "Eighth Term",
        zhValue = "第八任",
        jaValue = "第八任期"
    ),
    NINTH_TERM(
        koValue = "9선",
        enValue = "Ninth Term",
        zhValue = "第九任",
        jaValue = "第九任期"
    ),
    TENTH_TERM(
        koValue = "10선",
        enValue = "Tenth Term",
        zhValue = "第十任",
        jaValue = "第十任期"
    );

    companion object {
        val lookupByKoValue: Map<String, ElectionStatus> = ElectionStatus.entries.associateBy { it.koValue }
    }
}