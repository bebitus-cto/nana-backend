package com.nana.nana.domain.enums

import com.nana.nana.domain.feature.translation.config.Translatable
import kotlinx.serialization.Serializable

@Serializable
enum class LPMainStatus( // UI 단계
    override val koValue: String,
    override val enValue: String,
    override val zhValue: String,
    override val jaValue: String
) : Translatable {

    PROPOSED("발의", "Proposed", "提案", "提案"),
    OPEN_FOR_COMMENTS_IN_PROGRESS("진행 중 입법예고", "Open for Comments In Progress", "公开征求意见 进行中", "意見公募中"),
    OPEN_FOR_COMMENTS_ENDED("종료된 입법예고", "Open for Comments Ended", "公开征求意见 已结束", "意見公募終了"),
    COMMITTEE("상임위 심사", "Committee Review", "常务委员会审查", "常任委員会審議"),
    JUDICIAL("법사위 심사", "Judicial Committee Review", "司法委员会审查", "司法委員会審議"),
    PLENARY("본회의 심의", "Plenary Debate", "全体会议审议", "本会議審議"),
    GOVERNMENT_TRANSFER("정부이송", "Government Transfer", "政府移交", "政府移送"),
    PROMULGATION("공포", "Promulgation", "公布", "公布"), ;

    companion object {
        fun getOpenForCommentsStatus() = listOf(OPEN_FOR_COMMENTS_IN_PROGRESS, OPEN_FOR_COMMENTS_ENDED)
    }
}

@Serializable
enum class LPSubStatus(
    override val koValue: String,
    override val enValue: String,
    override val zhValue: String,
    override val jaValue: String
) : Translatable {
    COMPLETED("완료", "Completed", "完成", "完了"),
    UNDER_REVIEW("심사중", "Under Review", "审查中", "審査中"),
    IN_PROGRESS("진행중", "In Progress", "进行中", "進行中"),
    APPROVED("가결", "Approved", "通过", "可決"),
    REJECTED("부결", "Rejected", "否决", "否決"),
    DISCARDED("폐기", "Discarded", "废弃", "廃棄"),
    WITHDRAWN("철회", "Withdrawn", "撤回", "撤回"),
    PENDING("계류", "Pending", "待处理", "保留");


    companion object {
        fun getNegativeStatus() = listOf(PENDING) + getNegativeStatusExceptPending()
        fun getNegativeStatusExceptPending() = listOf(WITHDRAWN, REJECTED, DISCARDED)
    }
}

@Serializable
enum class LPStageResult(
    override val koValue: String,
    override val enValue: String,
    override val zhValue: String,
    override val jaValue: String
) : Translatable {
    COMPLETED("완료", "Completed", "完成", "完了"),
    UNDER_REVIEW("심사중", "Under Review", "审查中", "審査中"),
    IN_PROGRESS("진행중", "In Progress", "进行中", "進行中"),
    APPROVED("가결", "Approved", "通过", "可決"),
    REJECTED("부결", "Rejected", "否决", "否決"),
    DISCARDED("폐기", "Discarded", "废弃", "廃棄"),
    WITHDRAWN("철회", "Withdrawn", "撤回", "撤回"),
    PENDING("계류", "Pending", "待处理", "保留"),
    NO_RESULT("null", "No Result", "无结果", "結果なし"),
    NOT_APPLICABLE("알 수 없음", "Not Applicable", "不适用", "該当なし");

    companion object {
        fun toResult(resultStr: String?): LPStageResult {
            return if (resultStr.isNullOrBlank()) {
                NO_RESULT
            } else {
                entries.firstOrNull { resultStr.contains(it.koValue) }
                    ?: NOT_APPLICABLE
            }
        }
    }
}