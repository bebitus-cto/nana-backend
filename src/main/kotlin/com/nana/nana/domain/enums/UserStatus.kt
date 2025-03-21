package com.nana.nana.domain.enums

enum class UserStatus {
    ACTIVE,    // 최종 가입 완료: 이용약관 동의 완료
    INACTIVE,  // 비활성화
    REJECTED   // 가입 취소 또는 이용약관 동의 실패
}