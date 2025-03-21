package com.nana.nana.domain.feature.assemblysync.mp

import com.nana.nana.domain.enums.ElectionStatus
import com.nana.nana.domain.enums.Party
import com.nana.nana.domain.feature.assemblysync.lp.apiclient.TransApiClient
import com.nana.nana.domain.feature.assemblysync.mp.datamodel.MPSyncDataModel
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * diffMap -> 번역 대상이면서 변경이 될 수 있는 것
 * updateMap -> 번역과 상관 없는 값까지 추가(이넘 파싱, 날짜, Url, 번호 등)
 */
@Service
class SyncTransMPService(
    private val repository: SyncTransMPRepository,
    private val transApiClient: TransApiClient
) {

    private val logger: Logger = LoggerFactory.getLogger(SyncTransMPService::class.java)

    suspend fun batchTranslateMPsDetail(
        newMPs: List<MPSyncDataModel>,
        existingMps: Map<String, MPSyncDataModel>
    ): List<Pair<String, List<Map<String, Any?>>>> = coroutineScope {
        logger.info("국회의원 상세 정보 번역 배치작업 시작: 신규:${newMPs.count()}명, 기존:${existingMps.count()}명")

        val semaphore = Semaphore(10)
        val chunkedNewMPs = newMPs.chunked(10)

        val deferredTranslations = coroutineScope {
            chunkedNewMPs.map { chunk ->
                async(Dispatchers.IO) {
                    chunk.map { newMP ->
                        semaphore.withPermit {
                            val existingMP = existingMps[newMP.id]
                            val diffMap: MutableMap<String, String?> = if (existingMP != null) {
                                getChangedFields(newMP, existingMP)
                            } else {
                                getChangedFields(newMP)
                            }

                            val keysToTranslate = diffMap.keys.toList()
                            val textsToTranslate = keysToTranslate.map { diffMap[it].orEmpty() }

                            val translationResults = mutableListOf<Pair<String, Map<String, Any?>>>()

                            if (textsToTranslate.isNotEmpty()) {
                                // 1. 한국어 → 영어 번역 (영어 번역 결과를 캐싱)
                                val englishTexts = transApiClient.requestTranslation("", textsToTranslate, "ko", "en")
                                if (englishTexts.isNotEmpty() && englishTexts.count() == textsToTranslate.count()) {
                                    val englishTranslatedMap = keysToTranslate.zip(englishTexts) { key, translation ->
                                        key to translation.ifBlank { null }
                                    }.toMap()
                                    if (englishTranslatedMap.isNotEmpty()) {
                                        val updateMapEnglish = mutableMapOf<String, Any?>().apply {
                                            putAll(englishTranslatedMap)

                                            put("id", "${newMP.id}_en")
                                            put("extraId", newMP.extraId)

                                            // 번역이 필요 없는 필드들
                                            if (existingMP == null || newMP.gender != existingMP.gender) {
                                                put("gender", newMP.gender)
                                            }
                                            if (newMP.birthDate != existingMP?.birthDate) {
                                                put("birthDate", newMP.birthDate)
                                            }
                                            if (existingMP == null || newMP.officePhoneNumber != existingMP.officePhoneNumber) {
                                                put("officePhoneNumber", newMP.officePhoneNumber)
                                            }
                                            if (existingMP == null || newMP.officeEmailAddress != existingMP.officeEmailAddress) {
                                                put("officeEmail", newMP.officeEmailAddress)
                                            }
                                            if (existingMP == null || newMP.officialWebsiteUrl != existingMP.officialWebsiteUrl) {
                                                put("officialWebsiteUrl", newMP.officialWebsiteUrl)
                                            }
                                            if (existingMP == null || newMP.profilePictureUrl != existingMP.profilePictureUrl) {
                                                put("profilePictureUrl", newMP.profilePictureUrl)
                                            }
                                            if (existingMP == null || newMP.reElectionStatus != existingMP.reElectionStatus) {
                                                val translatedReElectionStatus =
                                                    ElectionStatus.lookupByKoValue[newMP.reElectionStatus]
                                                        ?.getTranslatedValue("en") ?: newMP.reElectionStatus
                                                put("reElectionStatus", translatedReElectionStatus)
                                            }
                                            if (existingMP == null || newMP.partyName != existingMP.partyName) {
                                                val tokens = newMP.partyName?.split("/")?.map { it.trim() }
                                                val translatedPartyNames =
                                                    tokens?.joinToString(separator = "/") { token ->
                                                        Party.lookupByKoValue[token]
                                                            ?.getTranslatedValue("en") ?: token
                                                    }
                                                put("partyName", translatedPartyNames)
                                            }
                                        }
                                        translationResults.add("en" to updateMapEnglish)
                                    }

                                    // 2. 영어 번역 결과를 소스로 하여, 영어를 제외한 다른 targetLanguage로 번역 요청
                                    targetLanguages.drop(1).forEach { targetLanguage ->
                                        val textsFromEnglish =
                                            keysToTranslate.map { englishTranslatedMap[it].orEmpty() }
                                        val translatedTexts =
                                            transApiClient.requestTranslation(
                                                textsToTranslate.joinToString(separator = ","),
                                                textsFromEnglish,
                                                "en",
                                                targetLanguage
                                            )
                                        if (translatedTexts.isNotEmpty() && translatedTexts.count() == keysToTranslate.count()) {
                                            val translatedMap =
                                                keysToTranslate.zip(translatedTexts) { key, translation ->
                                                    key to translation.ifBlank { null }
                                                }.toMap()
                                            if (translatedMap.isNotEmpty()) {
                                                val updateMap = mutableMapOf<String, Any?>().apply {
                                                    put("id", "${newMP.id}_$targetLanguage")
                                                    put("extraId", newMP.extraId)
                                                    putAll(translatedMap)

                                                    // ENUM 파싱 필드
                                                    if (existingMP == null || newMP.gender != existingMP.gender) {
                                                        put("gender", newMP.gender)
                                                    }
                                                    // 번역되지 않지만 파싱이 필요한 필드
                                                    if (newMP.birthDate != existingMP?.birthDate) {
                                                        put("birthDate", newMP.birthDate)
                                                    }
                                                    // 변경될 수 있는 필드들
                                                    if (existingMP == null || newMP.officePhoneNumber != existingMP.officePhoneNumber) {
                                                        put("officePhoneNumber", newMP.officePhoneNumber)
                                                    }
                                                    if (existingMP == null || newMP.officeEmailAddress != existingMP.officeEmailAddress) {
                                                        put("officeEmail", newMP.officeEmailAddress)
                                                    }
                                                    if (existingMP == null || newMP.officialWebsiteUrl != existingMP.officialWebsiteUrl) {
                                                        put("officialWebsiteUrl", newMP.officialWebsiteUrl)
                                                    }
                                                    if (existingMP == null || newMP.profilePictureUrl != existingMP.profilePictureUrl) {
                                                        put("profilePictureUrl", newMP.profilePictureUrl)
                                                    }
                                                    if (existingMP == null || newMP.reElectionStatus != existingMP.reElectionStatus) {
                                                        val translatedReElectionStatus =
                                                            ElectionStatus.lookupByKoValue[newMP.reElectionStatus]
                                                                ?.getTranslatedValue(targetLanguage)
                                                                ?: newMP.reElectionStatus
                                                        put("reElectionStatus", translatedReElectionStatus)
                                                    }
                                                    if (existingMP == null || newMP.partyName != existingMP.partyName) {
                                                        val tokens = newMP.partyName?.split("/")?.map { it.trim() }
                                                        val translatedPartyNames =
                                                            tokens?.joinToString(separator = "/") { token ->
                                                                Party.lookupByKoValue[token]
                                                                    ?.getTranslatedValue(targetLanguage) ?: token
                                                            }
                                                        put("partyName", translatedPartyNames)
                                                    }
                                                }
                                                translationResults.add(targetLanguage to updateMap)
                                            }
                                        }
                                    }
                                }
                            }
                            translationResults
                        }
                    }.flatten() // 각 청크 내의 모든 MP 번역 결과를 하나의 리스트로 만듦
                }
            }
        }

        val allTranslations: List<Pair<String, Map<String, Any?>>> = deferredTranslations.awaitAll().flatten()
        val grouped: Map<String, List<Map<String, Any?>>> = allTranslations.groupBy({ it.first }, { it.second })
        grouped.map { (lang, updates) -> lang to updates }
    }

    /**
     * history, birthplace - 2차에서 파싱 후 번역 예정
     */
    fun getChangedFields(
        newMP: MPSyncDataModel,
        existingMP: MPSyncDataModel? = null
    ): MutableMap<String, String?> {
        val diffMap = linkedMapOf<String, String?>()

        if (existingMP == null) {
//            diffMap["birthplace"] = newMP.birthplace
//            diffMap["history"] = newMP.history
            diffMap["positionName"] = newMP.positionName
            diffMap["electoralDistrictName"] = newMP.electoralDistrictName
            diffMap["electoralDistrictType"] = newMP.electoralDistrictType
            diffMap["committeeName"] = newMP.committeeName
            diffMap["electionEraco"] = newMP.electionEraco
            diffMap["officeRoomNumber"] = newMP.officeRoomNumber
            return diffMap
        }

//        if (newMP.birthplace != existingMP.birthplace) {
//            diffMap["birthplace"] = newMP.birthplace
//        }
//        if (newMP.history != existingMP.history) {
//            diffMap["history"] = newMP.history
//        }
        if (newMP.positionName != existingMP.positionName) {
            diffMap["positionName"] = newMP.positionName
        }
        if (newMP.electoralDistrictName != existingMP.electoralDistrictName) {
            diffMap["electoralDistrictName"] = newMP.electoralDistrictName
        }
        if (newMP.electoralDistrictType != existingMP.electoralDistrictType) {
            diffMap["electoralDistrictType"] = newMP.electoralDistrictType
        }
        if (newMP.committeeName != existingMP.committeeName) {
            diffMap["committeeName"] = newMP.committeeName
        }
        if (newMP.electionEraco != existingMP.electionEraco) {
            diffMap["electionEraco"] = newMP.electionEraco
        }
        if (newMP.officeRoomNumber != existingMP.officeRoomNumber) {
            diffMap["officeRoomNumber"] = newMP.officeRoomNumber
        }

        return diffMap
    }

    suspend fun batchUpsertMPsDetail(translatedMPs: List<Pair<String, List<Map<String, Any?>>>>) {
        repository.batchUpsertMPsDetail(translatedMPs)
    }

    suspend fun selectMPsByIds(ids: Set<String>, targetLanguage: String): List<MPSyncDataModel> {
        return repository.selectMPsByIds(ids, targetLanguage)
    }
}