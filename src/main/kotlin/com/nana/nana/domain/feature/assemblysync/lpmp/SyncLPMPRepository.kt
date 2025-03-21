package com.nana.nana.domain.feature.assemblysync.lpmp

import com.nana.nana.domain.enums.Party
import com.nana.nana.domain.enums.ProposerRole
import com.nana.nana.domain.feature.assemblysync.lp.datamodel.LPSyncDataModel
import com.nana.nana.domain.feature.assemblysync.mp.datamodel.MPSyncDataModel
import com.nana.nana.domain.feature.log.LogLPSyncRepository
import com.nana.nana.domain.feature.translation.config.TransConfig.allLanguages
import com.nana.nana.table.lpmp.LPsMPsTable
import com.nana.nana.table.mp.MPsKoTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class SyncLPMPRepository(
    private val logLPSyncRepository: LogLPSyncRepository
) {

    private val logger: Logger = LoggerFactory.getLogger(SyncLPMPRepository::class.java)

    suspend fun batchInsertIgnoreLPsMPs(
        newKeys: Set<String>,
        newLPs: List<LPSyncDataModel>,
        existingMPs: Map<String, MPSyncDataModel>,
    ) {

        val relations = mutableListOf<LPMPRelationDataModel>()
        val existingRelations = selectExistingRelations(newKeys)

        newLPs.forEach { lp ->
            if (lp.participants == null && lp.proposerKind?.contains("의원") == false) {
                newSuspendedTransaction {
                    logLPSyncRepository.logLPSyncError("발의자 데이터 없음: 의원 발의 아님 !(위원장 또는 정부 발의)", lp.id)
                }
            }

            lp.participants?.mainProposers?.forEach { mainProposer ->
                existingMPs[mainProposer]?.let { mp ->
                    if (existingRelations[lp.id] == null) {
                        val newRelation = LPMPRelationDataModel(lp.id, mp.id, ProposerRole.MAIN_PROPOSER)
                        relations.add(newRelation)
                    }
                } ?: run {
                    newSuspendedTransaction {
                        logLPSyncRepository.logLPSyncError("해당 이름의 (대표 발의자)를 찾을 수 없음: '$mainProposer'", id)
                    }
                }
            }

            lp.participants?.coProposers?.forEach { coProposer ->
                existingMPs[coProposer]?.let { mp ->
                    if (existingRelations[lp.id] == null) {
                        val newRelation = LPMPRelationDataModel(lp.id, mp.id, ProposerRole.CO_PROPOSER)
                        relations.add(newRelation)
                    }
                } ?: run {
                    newSuspendedTransaction {
                        logLPSyncRepository.logLPSyncError("해당 이름의 (공동 발의자)를 찾을 수 없음: '$coProposer'", id)
                    }
                }
            }

            lp.participants?.supporters?.forEach { supporter ->
                existingMPs[supporter]?.let { mp ->
                    if (existingRelations[lp.id] == null) {
                        val newRelation = LPMPRelationDataModel(lp.id, mp.id, ProposerRole.SUPPORTER)
                        relations.add(newRelation)
                    }
                } ?: run {
                    newSuspendedTransaction {
                        logLPSyncRepository.logLPSyncError("해당 이름의 (공동 발의자)를 찾을 수 없음: '$supporter'", id)
                    }
                }
            }
        }

        val resultRows = LPsMPsTable.batchInsert(
            data = relations,
            ignore = true/*희박한 확률 방지*/
        ) { (lpId, mpId, role) ->
            this[LPsMPsTable.lpId] = lpId
            this[LPsMPsTable.mpId] = mpId
            this[LPsMPsTable.role] = role
        }

        val insertedCount = resultRows.count()
        val relationsCount = LPsMPsTable.select { LPsMPsTable.lpId inList newKeys }.count().toInt()

        if (insertedCount == relationsCount) {
            logger.info("의안 - 의안 관계 짓기 ${resultRows.count()}건 완료")
        } else {
            logger.info("의안-의원 관계 맵핑이 정상적으로 완료됨: $insertedCount 건 추가됨")
        }
    }

    suspend fun selectExistingRelations(lpIds: Set<String>): Map<String, LPMPRelationDataModel> {
        return LPsMPsTable
            .select { LPsMPsTable.lpId inList lpIds }
            .map { row ->
                LPMPRelationDataModel(
                    lpId = row[LPsMPsTable.lpId],
                    mpId = row[LPsMPsTable.mpId],
                    role = row[LPsMPsTable.role]
                )
            }
            .associateBy { it.lpId }
    }

    fun getLPMainPartiesIfChanged(
        newLPs: List<LPSyncDataModel>,
        existingLPs: Map<String, LPSyncDataModel>
    ): Map<String, List<PartyRankDataModel>> {

        val filteredNewLPs = newLPs.filter { newLP ->
            val existingParty = existingLPs[newLP.id]?.leadingParty
            val newParty = newLP.leadingParty

            existingParty == null || (existingParty != newParty)
        }

        return if (filteredNewLPs.isNotEmpty()) {
            getLPMainParties(filteredNewLPs)
        } else {
            emptyMap()
        }
    }

    fun getLPMainParties(newLPs: List<LPSyncDataModel>): Map<String, List<PartyRankDataModel>> {
        val lpKeys = newLPs.map { it.id }.toSet()
        val lpDataMap = newLPs.associateBy { it.id }

        logger.warn("🔎 [STEP 1] 입력 받은 법률안 ID 개수: ${lpKeys.count()}, 목록: $lpKeys")

        // LPsMPsTable과 MPsKoTable 조인 후, 주요 정당 정보 조회
        val rawPartyData = LPsMPsTable
            .join(MPsKoTable, JoinType.INNER, LPsMPsTable.mpId, MPsKoTable.id)
            .slice(LPsMPsTable.lpId, MPsKoTable.partyName)
            .select { LPsMPsTable.lpId inList lpKeys }
            .toList()

        logger.warn("🔎 [STEP 2] 조회된 정당 데이터 개수: ${rawPartyData.size}")
        rawPartyData.forEach { row ->
            logger.debug("📌 조회된 데이터 - 법률안ID: ${row[LPsMPsTable.lpId]}, 정당명: ${row[MPsKoTable.partyName]}")
        }

        // 정당 데이터 그룹핑 및 카운트
        val partyCountsByLP: Map<String, Map<String, Int>> = rawPartyData
            .groupBy(
                keySelector = { row -> row[LPsMPsTable.lpId] },
                valueTransform = { row -> row[MPsKoTable.partyName]?.split("/")?.lastOrNull() ?: "undefined" }
            )
            .mapValues { (lpId, partyList) ->
                val countMap = partyList.groupingBy { it }.eachCount()
                logger.debug("📌 [STEP 3] 법률안 $lpId 정당별 개수: $countMap")
                countMap
            }

        val result = allLanguages.associateWith { mutableListOf<PartyRankDataModel>() }

        // 주요 정당을 정렬 후 추가
        partyCountsByLP.forEach { (lpId, partyCountMap) ->
            val sortedParties = partyCountMap.entries.sortedByDescending { it.value }.map { it.key }

            logger.debug("📌 [STEP 4] 주요정당 - 법률안ID: $lpId, 정렬된 정당 목록: $sortedParties")

            val lpSyncDataModel = lpDataMap[lpId] ?: run {
                logger.error("🚨 [ERROR] 법률안ID $lpId 가 newLPs에서 찾을 수 없음")
                return@forEach
            }

            val partyTranslations = sortedParties.map { party ->
                Party.lookupByKoValue[party.trim()] ?: throw IllegalArgumentException("매칭되는 정당 데이터 없음")
            }

            logger.debug("📌 [STEP 5] 정당 번역 결과 - 법률안ID: $lpId, 변환된 정당: $partyTranslations")

            allLanguages.forEach { lang ->
                val leadingParty = partyTranslations.getOrNull(0)?.getTranslatedValue(lang)
                val secondParty = partyTranslations.getOrNull(1)?.getTranslatedValue(lang)
                val thirdParty = partyTranslations.getOrNull(2)?.getTranslatedValue(lang)

                result[lang]?.add(
                    PartyRankDataModel(
                        lpSyncDataModel = lpSyncDataModel,
                        leadingParty = leadingParty,
                        secondParty = secondParty,
                        thirdParty = thirdParty
                    )
                )

                logger.debug("📌 [STEP 6] 추가된 PartyRankDataModel - 법률안ID: $lpId, 언어: $lang, 선두 정당: $leadingParty, 2위: $secondParty, 3위: $thirdParty")
            }
        }

        return result
    }
}
