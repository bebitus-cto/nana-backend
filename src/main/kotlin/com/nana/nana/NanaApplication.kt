package com.nana.nana

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


/**
 * 투표, 댓글, 결제,
 * 앱
 *
 * [화면]
 * 스플래쉬, 로그인, 회원가입, 본인인증
 * 법률안 리스트 및 상세(1~2번째 탭)
 * 국회의원 리스트 및 상세(3번째 탭)
 * 마이페이지(4번째 탭, 후원)
 *
 * [서버 API 연동]
 * 회원가입, 로그인
 * 본인 인증(KCB)
 * 인앱 결제 후원(단일, 정기)
 *
 * 법률안, 국회의원, 대통령 - 투표 댓글(답장)
 *
 * 리스트 페이징 - 법률안, 국회의원, 댓글, 대댓글
 *
 * 투표 - 법률안, 국회의원, 댓글, 대댓글
 *
 * 댓글 - 리스트 페이징, 법률안, 국회의원, 대통령, 대댓글, 정렬(인기순 시간순 최신순)
 *
 *
 * *앱 출시
 */
@SpringBootApplication
class NaNaApplication

fun main(args: Array<String>) {
    runApplication<NaNaApplication>(*args)
}