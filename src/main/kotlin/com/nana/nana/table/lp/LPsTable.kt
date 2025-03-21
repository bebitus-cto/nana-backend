package com.nana.nana.table.lp

import com.nana.nana.domain.enums.LPMainStatus
import com.nana.nana.domain.enums.LPStageResult
import com.nana.nana.domain.enums.LPSubStatus
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

abstract class LPsTable(name: String) : Table(name) {

    val id = varchar("id", 30) // 의안번호

    val extraId = varchar("extra_id", 255)

    val kind = varchar("kind", 100).nullable() // 의안 종류

    val name = text("name").nullable() // 의안명

    val isNew = bool("is_new").nullable()

    val nickname = varchar("nickname", 255).nullable()

    val proposerKind = varchar("proposer_kind", 255) // 발의 종류(의원, 정부, 위원장 등)

    val proposerOverview = varchar("proposer_overview", 255) // 발의 요약(의원 등 외, 법제사법위원장)

    val proposeSession = varchar("propose_session", 255)

    val noticeEndDate = date("notice_end_date").nullable() // 입법예고 종료일

    val proposalDate = date("proposal_date").nullable()

    val leadingParty = varchar("leading_party", 100).nullable() // 법안 주요 정당

    val committeeName = varchar("committee_name", 500).nullable() // 소관위원회 이름

    val committeeCommitDate = date("committee_commit_date").nullable() // 소관위 회부일

    val committeePresentationDate = date("committee_presentation_date").nullable() // 소관위 상정일

    val committeeProcessingDate = date("committee_processing_date").nullable() // 소관위 처리일

    val committeeResult =
        enumerationByName("committee_result", 50, LPStageResult::class).nullable() // 소관위 처리 결과

    val judicialReferralDate = date("judicial_referral_date").nullable() // 법사위 회부일

    val judicialPresentationDate = date("judicial_presentation_date").nullable() // 법사위 상정일

    val judicialProcessingDate = date("judicial_processing_date").nullable() // 법사위 처리일

    val judicialResult =
        enumerationByName("judicial_result", 50, LPStageResult::class).nullable() // 법사위 처리 결과

    val plenaryPresentationDate = date("plenary_presentation_date").nullable() // 본회의 상정일

    val plenaryResolutionDate = date("plenary_resolution_date").nullable() // 본회의 의결일

    val plenaryConferenceName = text("plenary_conference_name").nullable() // 본회의 회의명

    val plenaryResult =
        enumerationByName("plenary_result", 50, LPStageResult::class).nullable() // 본회의 처리 결과

    val governmentTransferDate = date("government_transfer_date").nullable() // 정부 이송일

    val promulgationLawName = text("promulgation_law_name").nullable() // 공포 법률명

    val promulgationDate = date("promulgation_date").nullable() // 공포일

    val promulgationNo = varchar("promulgation_no", 50).nullable() // 공포 번호

    val mainStatus =
        enumerationByName("main_status", 50, LPMainStatus::class).nullable() // 메인 상태

    val subStatus =
        enumerationByName("sub_status", 50, LPSubStatus::class).nullable() // 세부 상태

    val previewTexts = text("preview_texts").nullable()  // 법안 미리보기 텍스트

    override val primaryKey = PrimaryKey(id) // Primary Key 설정
}

object LPsKoTable : LPsTable("law_proposals_ko")
object LPsEnTable : LPsTable("law_proposals_en")
object LPsJaTable : LPsTable("law_proposals_ja")
object LPsZhTable : LPsTable("law_proposals_zh")

fun getLPsTable(languageCode: String? = "ko"): LPsTable {
    return when (languageCode) {
        "ko" -> LPsKoTable
        "en" -> LPsEnTable
        "ja" -> LPsJaTable
        "zh" -> LPsZhTable
        else -> throw IllegalArgumentException("지원되지 않는 언어 코드: $languageCode")
    }
}