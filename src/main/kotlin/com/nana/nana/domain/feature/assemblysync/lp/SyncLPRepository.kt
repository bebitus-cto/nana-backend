package com.nana.nana.domain.feature.assemblysync.lp

import com.nana.nana.domain.enums.LPMainStatus
import com.nana.nana.domain.enums.LPSubStatus
import com.nana.nana.domain.enums.ProposerRole
import com.nana.nana.config.ApiConfig.defaultJson
import com.nana.nana.domain.feature.assemblysync.config.SyncConfig.CURRENT_MP_ERACO
import com.nana.nana.domain.feature.assemblysync.lp.datamodel.LPSyncDataModel
import com.nana.nana.domain.feature.assemblysync.lp.datamodel.LPUrlDataModel
import com.nana.nana.domain.feature.assemblysync.lpmp.ParticipantSyncDataModel
import com.nana.nana.domain.feature.assemblysync.lpmp.PartyRankDataModel
import com.nana.nana.domain.feature.log.LogLPSyncRepository
import com.nana.nana.table.lp.LPUrlsTable
import com.nana.nana.table.lp.getLPsTable
import com.nana.nana.table.lpmp.LPsMPsTable
import com.nana.nana.table.mp.getMPsTable
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class SyncLPRepository(
    private val logLPSyncRepository: LogLPSyncRepository
) {

    private val logger: Logger = LoggerFactory.getLogger(SyncLPRepository::class.java)

    suspend fun batchUpsertLPsDetail(
        keys: Set<String>,
        dataModels: List<LPSyncDataModel>
    ): List<LPSyncDataModel> {

        try {

            val lpsTable = getLPsTable()

            val resultRows = lpsTable.batchUpsert(dataModels) { lp ->
                this[lpsTable.id] = lp.id
                this[lpsTable.extraId] = lp.extraId
                this[lpsTable.kind] = lp.kind
                this[lpsTable.name] = lp.name
                this[lpsTable.isNew] = lp.isNew
                this[lpsTable.noticeEndDate] = lp.noticeEndDate
                this[lpsTable.proposalDate] = lp.proposalDate
                this[lpsTable.proposerKind] = lp.proposerKind.orEmpty()
                this[lpsTable.proposerOverview] = lp.proposerOverview.orEmpty()
                this[lpsTable.proposeSession] = lp.proposeSession.orEmpty()
                this[lpsTable.committeeName] = lp.committeeName
                this[lpsTable.committeeCommitDate] = lp.committeeCommitDate
                this[lpsTable.committeePresentationDate] = lp.committeePresentationDate
                this[lpsTable.committeeProcessingDate] = lp.committeeProcessingDate
                this[lpsTable.committeeResult] = lp.committeeResult
                this[lpsTable.judicialReferralDate] = lp.judicialReferralDate
                this[lpsTable.judicialPresentationDate] = lp.judicialPresentationDate
                this[lpsTable.judicialProcessingDate] = lp.judicialProcessingDate
                this[lpsTable.judicialResult] = lp.judicialResult
                this[lpsTable.plenaryPresentationDate] = lp.plenaryPresentationDate
                this[lpsTable.plenaryResolutionDate] = lp.plenaryResolutionDate
                this[lpsTable.plenaryConferenceName] = lp.plenaryConferenceName
                this[lpsTable.plenaryResult] = lp.plenaryResult
                this[lpsTable.governmentTransferDate] = lp.governmentTransferDate
                this[lpsTable.promulgationLawName] = lp.promulgationLawName
                this[lpsTable.promulgationDate] = lp.promulgationDate
                this[lpsTable.promulgationNo] = lp.promulgationNo
                this[lpsTable.mainStatus] = lp.mainStatus
                this[lpsTable.subStatus] = lp.subStatus
                this[lpsTable.previewTexts] = defaultJson.encodeToString(lp.previewTexts)
            }

            val inputCount = dataModels.count()
            val insertedCount = resultRows.count()

            val dbDataMap = selectLPsByEracoAndIds(ids = keys)
            val dbData = dbDataMap.values.toList()
            val dbKeys = dbDataMap.keys
            val failedKeys = keys - dbKeys
            val failedData = dataModels.filter { lp ->
                val key = lp.id
                key in failedKeys
            }

            if (insertedCount == inputCount) {
                logger.warn("의안 데이터 배치처리 성공: ${dbData.count()}개")
                return dbData
            } else {
                logLPSyncRepository.logLPSyncError(
                    "일부 의안 데이터가 배치처리 중 Upsert 실패", failedData.joinToString(separator = ", ") { it.id }
                )
                return failedData.toList()
            }

        } catch (e: Exception) {
            newSuspendedTransaction {
                logLPSyncRepository.logLPSyncError("일부 의안 데이터가 초기 동기화 배치처리 중 Upsert 실패", "")
            }
            return emptyList()
        }
    }

    suspend fun batchUpsertLPsDetailOnlyStatus(
        keys: Set<String>,
        dataModels: List<LPSyncDataModel>,
    ): List<LPSyncDataModel> {

        try {

            val lpsTable = getLPsTable()

            val resultRows = lpsTable.batchUpsert(dataModels) { lp ->
                this[lpsTable.id] = lp.id
                this[lpsTable.extraId] = lp.extraId
//            this[lpsTable.kind] = lp.kind
//            this[lpsTable.name] = lp.name
//            this[lpsTable.isNew] = lp.isNew
                this[lpsTable.noticeEndDate] = lp.noticeEndDate
                this[lpsTable.proposalDate] = lp.proposalDate
//                this[lpsTable.proposeSession] = lp.proposeSession
                this[lpsTable.committeeName] = lp.committeeName
                this[lpsTable.committeeCommitDate] = lp.committeeCommitDate
                this[lpsTable.committeePresentationDate] = lp.committeePresentationDate
                this[lpsTable.committeeProcessingDate] = lp.committeeProcessingDate
                this[lpsTable.committeeResult] = lp.committeeResult
                this[lpsTable.judicialReferralDate] = lp.judicialReferralDate
                this[lpsTable.judicialPresentationDate] = lp.judicialPresentationDate
                this[lpsTable.judicialProcessingDate] = lp.judicialProcessingDate
                this[lpsTable.judicialResult] = lp.judicialResult
                this[lpsTable.plenaryPresentationDate] = lp.plenaryPresentationDate
                this[lpsTable.plenaryResolutionDate] = lp.plenaryResolutionDate
                this[lpsTable.plenaryConferenceName] = lp.plenaryConferenceName
                this[lpsTable.plenaryResult] = lp.plenaryResult
                this[lpsTable.governmentTransferDate] = lp.governmentTransferDate
                this[lpsTable.promulgationLawName] = lp.promulgationLawName
                this[lpsTable.promulgationDate] = lp.promulgationDate
                this[lpsTable.promulgationNo] = lp.promulgationNo
                this[lpsTable.mainStatus] = lp.mainStatus
                this[lpsTable.subStatus] = lp.subStatus
//            this[lpsTable.previewTexts] = defaultJson.encodeToString(lp.previewTexts)
            }

            val inputCount = dataModels.count()
            val insertedCount = resultRows.count()

            val dbDataMap = selectLPsByEracoAndIds(ids = keys)
            val dbData = dbDataMap.values.toList()
            val dbKeys = dbDataMap.keys
            val failedKeys = keys - dbKeys
            val failedData = dataModels.filter { lp ->
                val key = lp.id
                key in failedKeys
            }

            if (insertedCount == inputCount) {
                logger.warn("의안 데이터 추가 배치처리 성공: ${dbData.count()}개")
                return dbData
            } else {
                logLPSyncRepository.logLPSyncError(
                    "일부 의안 데이터가 추가 배치처리 중 Upsert 실패", failedData.joinToString(separator = ", ") { it.id }
                )
                return failedData.toList()
            }

        } catch (e: Exception) {
            newSuspendedTransaction {
                logLPSyncRepository.logLPSyncError("일부 의안 데이터가 배치처리 중 Upsert 실패", "")
            }
            return emptyList()
        }
    }

    fun selectLPsOnlyNeededUpdate(eraco: Int = CURRENT_MP_ERACO): Map<String, LPSyncDataModel> {
        val lpsTable = getLPsTable()
        val lpUrlTable = LPUrlsTable
        val mpsTable = getMPsTable()

        // 필수 조건: 무조건 적용되어야 함.
        val isCurrentEraco = lpsTable.id like "${eraco}%"
        val isPositiveStatus = lpsTable.subStatus notInList LPSubStatus.getNegativeStatusExceptPending()

        // 조건 1: 행의 mainStatus가 입법예고 상태이면, noticeEndDate가 만료되어야 한다.
        // (즉, noticeEndDate가 오늘 이하여야 한다.)
        val openForCommentsExpiredCondition = (lpsTable.mainStatus inList LPMainStatus.getOpenForCommentsStatus()) and
                (lpsTable.noticeEndDate lessEq LocalDate.now())

        // 조건 2: 행의 mainStatus가 입법예고 상태가 아니라면, mainStatus가 PROMULGATION 상태가 아니어야 한다.
        val nonOpenForCommentsCondition = (lpsTable.mainStatus notInList LPMainStatus.getOpenForCommentsStatus()) and
                (lpsTable.mainStatus neq LPMainStatus.PROMULGATION)

        // 최종 조건: 무조건 isCurrentEraco와 isPositiveStatus는 적용되고,
        // 그리고 입법예고 상태이면 openForCommentsExpiredCondition, 아니면 nonOpenForCommentsCondition을 만족해야 한다.
        val finalCondition =
            isCurrentEraco and isPositiveStatus and (openForCommentsExpiredCondition or nonOpenForCommentsCondition)

        val query = lpsTable
            .join(lpUrlTable, JoinType.LEFT, lpsTable.id, lpUrlTable.lpId)
            .join(LPsMPsTable, JoinType.LEFT, lpsTable.id, LPsMPsTable.lpId)
            .join(mpsTable, JoinType.LEFT, LPsMPsTable.mpId, mpsTable.id)
            .select { finalCondition }

        // LP ID별로 그룹핑 (하나의 LP에 대해 여러 행이 있을 수 있음)
        val grouped: Map<String, List<ResultRow>> = query.groupBy { it[lpsTable.id] }

        return grouped.mapValues { (lpId, rows) ->
            val firstRow = rows.first()

            // 역할별 MP 이름을 한 번의 순회로 수집
            val mainProposers = mutableListOf<String>()
            val coProposers = mutableListOf<String>()
            val supporters = mutableListOf<String>()
            rows.forEach { row ->
                val role = row[LPsMPsTable.role]
                val mpName = row[mpsTable.name]
                if (mpName != null) {
                    when (role) {
                        ProposerRole.MAIN_PROPOSER -> mainProposers.add(mpName)
                        ProposerRole.CO_PROPOSER -> coProposers.add(mpName)
                        ProposerRole.SUPPORTER -> supporters.add(mpName)
                    }
                }
            }
            val participants = ParticipantSyncDataModel(
                mainProposers = mainProposers.distinct(),
                coProposers = coProposers.distinct(),
                supporters = supporters.distinct()
            )

            // LP URL 데이터는 LEFT JOIN 결과에 따라 일부 컬럼이 null일 수 있으므로 그대로 사용합니다.
            val lpUrlDataModel = LPUrlDataModel(
                lpId = firstRow[lpUrlTable.lpId],
                linkUrl = firstRow[lpUrlTable.linkUrl],
                rawHWPUrl = firstRow[lpUrlTable.lpRawHWPFileUrl],
                rawPDFUrl = firstRow[lpUrlTable.lpRawPDFFileUrl],
                gcpHWPUrl = firstRow[lpUrlTable.lpGCPHWPFileUrl],
                gcpPDFUrl = firstRow[lpUrlTable.lpGCPPDFFileUrl]
            )

            LPSyncDataModel(
                id = firstRow[lpsTable.id],
                extraId = firstRow[lpsTable.extraId],
                kind = firstRow[lpsTable.kind].orEmpty(),
                name = firstRow[lpsTable.name].orEmpty(),
                isNew = firstRow[lpsTable.isNew],
                nickname = firstRow[lpsTable.nickname],
                proposerKind = firstRow[lpsTable.proposerKind],
                proposerOverview = firstRow[lpsTable.proposerOverview],
                proposeSession = firstRow[lpsTable.proposeSession],
                proposalDate = firstRow[lpsTable.proposalDate],
                noticeEndDate = firstRow[lpsTable.noticeEndDate],
                committeeName = firstRow[lpsTable.committeeName],
                committeeCommitDate = firstRow[lpsTable.committeeCommitDate],
                committeePresentationDate = firstRow[lpsTable.committeePresentationDate],
                committeeProcessingDate = firstRow[lpsTable.committeeProcessingDate],
                committeeResult = firstRow[lpsTable.committeeResult],
                judicialReferralDate = firstRow[lpsTable.judicialReferralDate],
                judicialPresentationDate = firstRow[lpsTable.judicialPresentationDate],
                judicialProcessingDate = firstRow[lpsTable.judicialProcessingDate],
                judicialResult = firstRow[lpsTable.judicialResult],
                plenaryPresentationDate = firstRow[lpsTable.plenaryPresentationDate],
                plenaryResolutionDate = firstRow[lpsTable.plenaryResolutionDate],
                plenaryConferenceName = firstRow[lpsTable.plenaryConferenceName],
                plenaryResult = firstRow[lpsTable.plenaryResult],
                governmentTransferDate = firstRow[lpsTable.governmentTransferDate],
                promulgationLawName = firstRow[lpsTable.promulgationLawName],
                promulgationDate = firstRow[lpsTable.promulgationDate],
                promulgationNo = firstRow[lpsTable.promulgationNo],
                mainStatus = firstRow[lpsTable.mainStatus],
                subStatus = firstRow[lpsTable.subStatus],
                previewTexts = firstRow[lpsTable.previewTexts].let { defaultJson.decodeFromString(it.orEmpty()) },
                participants = participants,
                lpUrlDataModel = lpUrlDataModel
            )
        }
    }

    fun selectLPsByEracoAndIds(
        ids: Set<String> = emptySet(),
        eraco: Int = CURRENT_MP_ERACO
    ): Map<String, LPSyncDataModel> {

        val lpsTable = getLPsTable()
        val lpUrlTable = LPUrlsTable
        val mpsTable = getMPsTable()
        val eracoFilter = lpsTable.id like "${CURRENT_MP_ERACO}%"
        val idFilter = if (ids.isNotEmpty()) lpsTable.id inList ids else Op.TRUE

        // LP, LP URL, LP-MP 매핑, MP 테이블을 조인하여 한 번에 데이터를 조회
        val query = lpsTable
            .join(lpUrlTable, JoinType.LEFT, lpsTable.id, lpUrlTable.lpId)
            .join(LPsMPsTable, JoinType.LEFT, lpsTable.id, LPsMPsTable.lpId)
            .join(mpsTable, JoinType.LEFT, LPsMPsTable.mpId, mpsTable.id)
            .select { (eracoFilter) and (idFilter) }

        // LP ID별로 그룹핑 (각 그룹은 같은 법안(LP)에 해당하는 여러 row로 구성됨)
        val grouped: Map<String, List<ResultRow>> = query.groupBy { it[lpsTable.id] }

        // 각 LP 그룹에 대해 한 번만 순회하여 MP 역할별로 이름을 분류하고, 첫 번째 row로 LP 기본 정보를 채웁니다.
        return grouped.mapValues { (lpId, rows) ->
            // 첫 번째 row에서 LP 테이블의 기본 정보를 추출
            val firstRow = rows.first()

            val mainProposers = mutableListOf<String>()
            val coProposers = mutableListOf<String>()
            val supporters = mutableListOf<String>()
            rows.forEach { row ->
                val role = row[LPsMPsTable.role]
                val mpName = row[mpsTable.name]
                if (mpName != null) {
                    when (role) {
                        ProposerRole.MAIN_PROPOSER -> mainProposers.add(mpName)
                        ProposerRole.CO_PROPOSER -> coProposers.add(mpName)
                        ProposerRole.SUPPORTER -> supporters.add(mpName)
                    }
                }
            }
            // 중복 제거 후 ParticipantSyncDataModel 생성
            val participants = ParticipantSyncDataModel(
                mainProposers = mainProposers.distinct(),
                coProposers = coProposers.distinct(),
                supporters = supporters.distinct()
            )

            // LP 기본 정보와 함께 participants, LP URL 정보를 채워서 LPSyncDataModel 생성
            LPSyncDataModel(
                id = firstRow[lpsTable.id],
                extraId = firstRow[lpsTable.extraId],
                kind = firstRow[lpsTable.kind].orEmpty(),
                name = firstRow[lpsTable.name].orEmpty(),
                isNew = firstRow[lpsTable.isNew],
                nickname = firstRow[lpsTable.nickname],
                proposerKind = firstRow[lpsTable.proposerKind],
                proposerOverview = firstRow[lpsTable.proposerOverview],
                proposeSession = firstRow[lpsTable.proposeSession],
                proposalDate = firstRow[lpsTable.proposalDate],
                noticeEndDate = firstRow[lpsTable.noticeEndDate],
                committeeName = firstRow[lpsTable.committeeName],
                committeeCommitDate = firstRow[lpsTable.committeeCommitDate],
                committeePresentationDate = firstRow[lpsTable.committeePresentationDate],
                committeeProcessingDate = firstRow[lpsTable.committeeProcessingDate],
                committeeResult = firstRow[lpsTable.committeeResult],
                judicialReferralDate = firstRow[lpsTable.judicialReferralDate],
                judicialPresentationDate = firstRow[lpsTable.judicialPresentationDate],
                judicialProcessingDate = firstRow[lpsTable.judicialProcessingDate],
                judicialResult = firstRow[lpsTable.judicialResult],
                plenaryPresentationDate = firstRow[lpsTable.plenaryPresentationDate],
                plenaryResolutionDate = firstRow[lpsTable.plenaryResolutionDate],
                plenaryConferenceName = firstRow[lpsTable.plenaryConferenceName],
                plenaryResult = firstRow[lpsTable.plenaryResult],
                governmentTransferDate = firstRow[lpsTable.governmentTransferDate],
                promulgationLawName = firstRow[lpsTable.promulgationLawName],
                promulgationDate = firstRow[lpsTable.promulgationDate],
                promulgationNo = firstRow[lpsTable.promulgationNo],
                mainStatus = firstRow[lpsTable.mainStatus],
                subStatus = firstRow[lpsTable.subStatus],
                previewTexts = firstRow[lpsTable.previewTexts].let { defaultJson.decodeFromString(it.orEmpty()) },
                participants = participants,
                lpUrlDataModel = LPUrlDataModel(
                    lpId = firstRow[lpUrlTable.lpId],
                    linkUrl = firstRow[lpUrlTable.linkUrl],
                    rawHWPUrl = firstRow[lpUrlTable.lpRawHWPFileUrl],
                    rawPDFUrl = firstRow[lpUrlTable.lpRawPDFFileUrl],
                    gcpHWPUrl = firstRow[lpUrlTable.lpGCPHWPFileUrl],
                    gcpPDFUrl = firstRow[lpUrlTable.lpGCPPDFFileUrl],
                ),
            )
        }
    }

    fun batchUpsertLPleadingParty(partyRanksMap: Map<String, List<PartyRankDataModel>>) {
        allLanguages.forEach { lang ->
            val lpsTable = getLPsTable(lang)

            val partyRankList = partyRanksMap[lang] ?: emptyList()

            lpsTable.batchUpsert(partyRankList) { partyRank ->
                this[lpsTable.id] = partyRank.lpSyncDataModel.id
                this[lpsTable.extraId] = partyRank.lpSyncDataModel.extraId
                this[lpsTable.proposerKind] = partyRank.lpSyncDataModel.proposerKind.orEmpty()
                this[lpsTable.proposerOverview] = partyRank.lpSyncDataModel.proposerOverview.orEmpty()
                this[lpsTable.proposeSession] = partyRank.lpSyncDataModel.proposeSession.orEmpty()
                this[lpsTable.leadingParty] = partyRank.leadingParty
            }

            logger.info("정당 배치 업데이트: $lang, count: ${partyRankList.count()}")
        }
    }

    fun batchUpsertLPNicknames(koNicknames: Map<LPSyncDataModel, String>) {
        if (koNicknames.isEmpty()) {
            logger.info("📌 업로드할 한국어 닉네임 데이터가 없음")
            return
        }

        val lpsTable = getLPsTable()

        try {
            val rows = lpsTable.batchUpsert(koNicknames.entries) { (lp, koNickname) ->
                this[lpsTable.id] = lp.id
                this[lpsTable.extraId] = lp.extraId
                this[lpsTable.proposerKind] = lp.proposerKind.orEmpty()
                this[lpsTable.proposerOverview] = lp.proposerOverview.orEmpty()
                this[lpsTable.proposeSession] = lp.proposeSession.orEmpty()
                this[lpsTable.nickname] = koNickname
            }

            logger.info("✅ 한국어 닉네임 batch upsert 완료! (${rows.count()}건)")
        } catch (e: Exception) {
            logger.error("❌ 한국어 닉네임 upsert 오류: ${e.message}", e)
        }
    }

    suspend fun fetchLeftoffNicknameLPs(): List<LPSyncDataModel> {
        val lpsTable = getLPsTable("ko")

        val rows = lpsTable
            .select { lpsTable.nickname.isNull() or (lpsTable.nickname eq "") or (lpsTable.nickname regexp "^[0-9]+$") }
            .map { row ->
                LPSyncDataModel(
                    id = row[lpsTable.id],
                    extraId = row[lpsTable.extraId],
                    kind = row[lpsTable.kind].orEmpty(),
                    name = row[lpsTable.name].orEmpty(),
                    isNew = row[lpsTable.isNew],
                    nickname = row[lpsTable.nickname],
                    proposerKind = row[lpsTable.proposerKind],
                    proposerOverview = row[lpsTable.proposerOverview],
                    proposeSession = row[lpsTable.proposeSession],
                    proposalDate = row[lpsTable.proposalDate],
                    noticeEndDate = row[lpsTable.noticeEndDate],
                    committeeName = row[lpsTable.committeeName],
                    committeeCommitDate = row[lpsTable.committeeCommitDate],
                    committeePresentationDate = row[lpsTable.committeePresentationDate],
                    committeeProcessingDate = row[lpsTable.committeeProcessingDate],
                    committeeResult = row[lpsTable.committeeResult],
                    judicialReferralDate = row[lpsTable.judicialReferralDate],
                    judicialPresentationDate = row[lpsTable.judicialPresentationDate],
                    judicialProcessingDate = row[lpsTable.judicialProcessingDate],
                    judicialResult = row[lpsTable.judicialResult],
                    plenaryPresentationDate = row[lpsTable.plenaryPresentationDate],
                    plenaryResolutionDate = row[lpsTable.plenaryResolutionDate],
                    plenaryConferenceName = row[lpsTable.plenaryConferenceName],
                    plenaryResult = row[lpsTable.plenaryResult],
                    governmentTransferDate = row[lpsTable.governmentTransferDate],
                    promulgationLawName = row[lpsTable.promulgationLawName],
                    promulgationDate = row[lpsTable.promulgationDate],
                    promulgationNo = row[lpsTable.promulgationNo],
                    mainStatus = row[lpsTable.mainStatus],
                    subStatus = row[lpsTable.subStatus],
                    previewTexts = emptyList(),
                    participants = null,
                    lpUrlDataModel = LPUrlDataModel()
                )
            }

        logger.info("✅ 누락된 닉네임을 가진 법률안 ${rows.size}건 조회 완료")
        logger.info("✅ 누락된 닉네임을 가진 법률안 별명 목록: ${rows.map { it.nickname }}")
        return rows
    }

    suspend fun fetchLeftoffLeadingPartyLPs(): List<LPSyncDataModel> {
        val lpsTable = getLPsTable("ko")

        val rows = lpsTable
            .select { (lpsTable.leadingParty.isNull()) and (lpsTable.proposerKind eq "의원") }
            .map { row ->
                LPSyncDataModel(
                    id = row[lpsTable.id],
                    extraId = row[lpsTable.extraId],
                    kind = row[lpsTable.kind].orEmpty(),
                    name = row[lpsTable.name].orEmpty(),
                    isNew = row[lpsTable.isNew],
                    nickname = row[lpsTable.nickname],
                    proposerKind = row[lpsTable.proposerKind],
                    proposerOverview = row[lpsTable.proposerOverview],
                    proposeSession = row[lpsTable.proposeSession],
                    proposalDate = row[lpsTable.proposalDate],
                    noticeEndDate = row[lpsTable.noticeEndDate],
                    committeeName = row[lpsTable.committeeName],
                    committeeCommitDate = row[lpsTable.committeeCommitDate],
                    committeePresentationDate = row[lpsTable.committeePresentationDate],
                    committeeProcessingDate = row[lpsTable.committeeProcessingDate],
                    committeeResult = row[lpsTable.committeeResult],
                    judicialReferralDate = row[lpsTable.judicialReferralDate],
                    judicialPresentationDate = row[lpsTable.judicialPresentationDate],
                    judicialProcessingDate = row[lpsTable.judicialProcessingDate],
                    judicialResult = row[lpsTable.judicialResult],
                    plenaryPresentationDate = row[lpsTable.plenaryPresentationDate],
                    plenaryResolutionDate = row[lpsTable.plenaryResolutionDate],
                    plenaryConferenceName = row[lpsTable.plenaryConferenceName],
                    plenaryResult = row[lpsTable.plenaryResult],
                    governmentTransferDate = row[lpsTable.governmentTransferDate],
                    promulgationLawName = row[lpsTable.promulgationLawName],
                    promulgationDate = row[lpsTable.promulgationDate],
                    promulgationNo = row[lpsTable.promulgationNo],
                    mainStatus = row[lpsTable.mainStatus],
                    subStatus = row[lpsTable.subStatus],
                    previewTexts = emptyList(),
                    participants = null,
                    lpUrlDataModel = LPUrlDataModel()
                )
            }

        logger.info("✅ 누락된 대표정당을 가진 법률안 ${rows.size}건 조회 완료")
        logger.info("✅ 누락된 대표정당을 가진 법률안 별명 목록: ${rows.map { it.nickname }}")
        return rows
    }
}