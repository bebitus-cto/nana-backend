package com.nana.nana.table.lpmp

import com.nana.nana.domain.enums.ProposerRole
import org.jetbrains.exposed.sql.Table

// 의안과 국회의원을 잇는 다대다 테이블 정의
object LPsMPsTable : Table("lps_mps") {

    val lpId = varchar("lp_id", 10)

    val mpId = varchar("mp_id", 30)

    val role = enumerationByName("role", 50, ProposerRole::class) // 역할 이넘 필드 (발의, 찬성)

    override val primaryKey = PrimaryKey(lpId, mpId)
}
