package com.nana.nana.domain.enums

enum class LPKind(val description: String) {
    CONSTITUTION_AMENDMENT("헌법개정안"),
    LEGISLATION_BILL("법률안"),
    BUDGET_BILL("예산안"),
    FUND_OPERATION_PLAN("기금운용계획안"),
    BTL_LIMIT_BILL("임대형 민자사업 한도액안"),
    SETTLEMENT_BILL("결산안"),
    APPROVAL_BILL("동의안"),
    CONFIRMATION_BILL("승인안"),
    RESOLUTION_BILL("결의안"),
    RECOMMENDATION_BILL("건의안"),
    RULES_BILL("규칙안"),
    ELECTION_BILL("선출안"),
    IMPORTANT_MOTION("중요동의안"),
    DISCIPLINE_BILL("의원징계안"),
    QUALIFICATION_REVIEW("의원자격심사안"),
    HEARING_REQUEST("인사청문요청안"),
    COMMITTEE_NOMINATION("각종 위원 위촉안"),
    NATIONAL_INVESTIGATION_REQUEST("국정조사요구안")
}