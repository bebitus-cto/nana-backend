package com.nana.nana.domain.enums

enum class LawProposalKind(val description: String) {
    CONSTITUTION_AMENDMENT("헌법개정"),
    LEGISLATION_BILL("법률"),
    BUDGET_BILL("예산"),
    FUND_OPERATION_PLAN("기금운용계획"),
    BTL_LIMIT_BILL("임대형 민자사업 한도액"),
    SETTLEMENT_BILL("결산"),
    APPROVAL_BILL("동의"),
    CONFIRMATION_BILL("승인"),
    RESOLUTION_BILL("결의"),
    RECOMMENDATION_BILL("건의"),
    RULES_BILL("규칙"),
    ELECTION_BILL("선출"),
    IMPORTANT_MOTION("중요동의"),
    DISCIPLINE_BILL("의원징계"),
    QUALIFICATION_REVIEW("의원자격심사"),
    HEARING_REQUEST("인사청문요청"),
    COMMITTEE_NOMINATION("각종 위원 위촉"),
    NATIONAL_INVESTIGATION_REQUEST("국정조사요구")
}