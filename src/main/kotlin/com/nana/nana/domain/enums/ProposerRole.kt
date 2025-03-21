package com.nana.nana.domain.enums

import com.nana.nana.domain.feature.translation.config.Translatable
import kotlinx.serialization.Serializable

@Serializable
enum class ProposerRole(
    override val koValue: String,
    override val enValue: String,
    override val zhValue: String,
    override val jaValue: String
) : Translatable {

    MAIN_PROPOSER(
        "대표발의자",
        "Main Proposer",
        "代表发议者",
        "代表発議者"
    ),
    CO_PROPOSER(
        "공동발의자",
        "Co-Proposer",
        "共同发议者",
        "共同発議者"
    ),
    SUPPORTER(
        "찬성",
        "Supporter",
        "赞成",
        "賛成"
    );
}