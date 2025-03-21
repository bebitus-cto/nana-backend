package com.nana.nana.table.lp

import org.jetbrains.exposed.sql.Table

/**
 * 법안 메타데이터 테이블 (법안 관련 URL)
 */
object LPUrlsTable : Table("lp_urls") {

    val lpId = varchar("lp_id", 30) // 의안 번호
    val linkUrl = text("link_url").nullable()          // 법안 상세 페이지 URL
    val lpRawHWPFileUrl = text("lp_raw_hwp_file_url").nullable()
    val lpRawPDFFileUrl = text("lp_raw_pdf_file_url").nullable()
    val lpGCPHWPFileUrl = text("lp_gcp_hwp_file_url").nullable()
    val lpGCPPDFFileUrl = text("lp_gcp_pdf_file_url").nullable()

    override val primaryKey = PrimaryKey(lpId)
}