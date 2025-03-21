package com.nana.nana.util

import com.nana.nana.domain.feature.assemblysync.lp.datamodel.LPUrlDataModel
import com.nana.nana.domain.feature.assemblysync.lpmp.ParticipantSyncDataModel
import com.nana.nana.domain.feature.assemblysync.mp.datamodel.MPSyncDataModel
import com.nana.nana.domain.feature.log.LogLPSyncRepository
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WebParser(
    private val logLPSyncRepository: LogLPSyncRepository
) {

    private val logger: Logger = LoggerFactory.getLogger(WebParser::class.java)

    suspend fun extractLPFileRawUrls(id: String, url: String?): LPUrlDataModel {
        if (url == null) {
            newSuspendedTransaction {
                logLPSyncRepository.logLPSyncError("의안 웹사이트 linkUrl이 없음", id)
            }
            return LPUrlDataModel(
                lpId = id,
                linkUrl = null,
            )
        }

        try {
            val document: Document = try {
                Jsoup.connect(url).get()
            } catch (e: Exception) {
                newSuspendedTransaction {
                    logLPSyncRepository.logLPSyncError("파싱 중 에러 발생 extractLPFileRawUrls", id)
                }
                return LPUrlDataModel(
                    lpId = id,
                    linkUrl = url,
                )
            }

            // 2. HWP 및 PDF 다운로드 링크 추출
            val fileElements = document.select("a[href*=openBillFile]")

            if (fileElements.isEmpty()) {
                newSuspendedTransaction {
                    logLPSyncRepository.logLPSyncError("pdf/hwp 문서 다운로드 링크가 없음", id)
                }
                return LPUrlDataModel(
                    lpId = id,
                    linkUrl = url,
                )
            }

            var rawHWPUrl: String? = null
            var rawPDFUrl: String? = null

            // 3. 각 파일의 다운로드 링크 파싱
            val fileRegex = Regex("openBillFile\\('([^']+)',\\s*'([^']+)',\\s*'([01])'\\)")

            for (element in fileElements) {
                val matchResult = fileRegex.find(element.attr("href")) ?: continue

                val baseUrl = matchResult.groups[1]?.value
                val bookId = matchResult.groups[2]?.value
                val fileType = matchResult.groups[3]?.value

                if (baseUrl != null && bookId != null && fileType != null) {
                    val downloadUrl = "$baseUrl?bookId=$bookId&type=$fileType"

                    when (fileType) {
                        "0" -> rawHWPUrl = downloadUrl
                        "1" -> rawPDFUrl = downloadUrl
                    }
                }
            }

            logger.info("hwp 다운로드 Url: $rawHWPUrl")
            logger.info("pdf 다운로드 Url: $rawPDFUrl")

            return LPUrlDataModel(
                lpId = id,
                linkUrl = url,
                rawHWPUrl = rawHWPUrl,
                rawPDFUrl = rawPDFUrl
            )

        } catch (e: Exception) {
            newSuspendedTransaction {
                logLPSyncRepository.logLPSyncError("파일 다운로드 링크 추출 중 오류 발생: ${e.message}", id)
            }
            return LPUrlDataModel(
                lpId = id,
                linkUrl = url,
            )
        }
    }

    /**
     * HTML 문서에서 섹션별 제목과 본문을 추출하는 함수
     * @param url 웹페이지 URL
     * @return List<Pair<String, List<String>>> (제목, 본문 리스트)
     */
    suspend fun extractPreviewText(id: String, url: String?): List<LawProposalPreviewText>? {

        if (url == null) {
            logger.error("url이 없습니다.")
            return null
        }


        return try {
            val document: Document = try {
                Jsoup.connect(url).get()
            } catch (e: Exception) {
                newSuspendedTransaction {
                    logLPSyncRepository.logLPSyncError("파싱 중 에러 발생 extractPreviewText: ${e.message}", id)
                }
                return null
            }

            val sectionElements = document.select("div.textType02")
            val sections = mutableListOf<LawProposalPreviewText>()

            for (element in sectionElements) {
                val rawContent =
                    element.html().split("<br>")
                        .map { Jsoup.parse(it).text().trim() }
                        .filter { it.isNotBlank() }

                if (rawContent.isEmpty()) {
                    continue
                }

                // 현재 섹션의 제목
                var currentTitleType: PreviewTextTitleType? = null
                // 현재 섹션의 본문 내용
                val currentContents = mutableListOf<String>()

                for ((index, line) in rawContent.withIndex()) {
                    val matchedType = PreviewTextTitleType.matchTitleType(line)

                    if (matchedType != null) {
                        // 기존 섹션 저장
                        if (currentTitleType != null) {
                            sections.add(
                                LawProposalPreviewText(
                                    titleType = currentTitleType,
                                    contents = currentContents
                                )
                            )
                            currentContents.clear()
                        }
                        currentTitleType = matchedType
                    } else {
                        currentContents.add(line)
                    }

                    // 마지막 섹션은 바로 저장
                    if (index == rawContent.lastIndex && currentTitleType != null) {
                        sections.add(
                            LawProposalPreviewText(
                                titleType = currentTitleType,
                                contents = currentContents
                            )
                        )
                    }
                }
            }

            logger.info("의안요약 sections: $sections")

            sections.toList()

        } catch (e: Exception) {
            newSuspendedTransaction {
                logLPSyncRepository.logLPSyncError("html 파싱 중 알 수 없는 오류 발생: ${e.message}", id)
            }
            null
        }
    }

    suspend fun extractParticipants(
        id: String,
        url: String?,
        existingMPs: Map<String, MPSyncDataModel>
    ): ParticipantSyncDataModel? {
        logger.debug("url: $url")
        if (url == null) return null

        val document: Document = try {
            Jsoup.connect(url).get()
        } catch (e: Exception) {
            newSuspendedTransaction {
                logLPSyncRepository.logLPSyncError("파싱 중 에러 발생 extractParticipants: ${e.message}", id)
            }
            return null
        }

        // 1. 대표 발의자 관련 텍스트 추출
        val representativeText = document.select("p.textType01").text()

        // 개선된 정규표현식:
        // - [id] : 법안 ID
        // - title : 법안 제목 (최소화하여 캡처)
        // - names : 괄호 내, 최초의 "등" 또는 "외" 전까지의 대표 발의자 이름들
        // - sep1/count1 : 첫번째 (있다면) "등" 또는 "외"와 숫자
        // - sep2/count2 : 두번째 (있다면) "등" 또는 "외"와 숫자
        val representativeRegex = Regex(
            """\[(?<id>\d+)\](?<title>.*?)\((?<names>[^등외]+)(?:\s+(?<sep1>[등외])\s?(?<count1>\d+)인)?(?:\s+(?<sep2>[등외])\s?(?<count2>\d+)인)?\)"""
        )
        val representativeMatch = representativeRegex.find(representativeText)

        // 디버깅: 캡처된 그룹들을 확인
        val capNames = representativeMatch?.groups?.get("names")?.value?.trim() ?: ""
        val capSep1 = representativeMatch?.groups?.get("sep1")?.value
        val capCount1 = representativeMatch?.groups?.get("count1")?.value
        val capSep2 = representativeMatch?.groups?.get("sep2")?.value
        val capCount2 = representativeMatch?.groups?.get("count2")?.value
        logger.info("RepresentativeText: $representativeText")
        logger.info("Captured names: '$capNames', sep1: '$capSep1', count1: '$capCount1', sep2: '$capSep2', count2: '$capCount2'")

        // 2. 대표 발의자 이름 추출 및 후처리
        // group "names"에서 추출된 문자열(예: "이광희의원")를 분리(구분자에 따라 여러 명일 수 있음)
        val mainProposerNames: List<String> = if (capNames.isNotEmpty()) {
            capNames.split(Regex("[ㆍ·•●‧・,\\s]+"))
                .map { it.trim().replace("의원", "") }
                .filter { it.isNotEmpty() }
        } else {
            emptyList()
        }

        // 3. DB 데이터를 결합하여 대표 발의자 보강 (예: "이광희" → "이광희(더불어민주당, 서울 금천구)")
        val enhancedMainProposers: List<String> = mainProposerNames.map { name ->
            existingMPs[name]?.let { mp ->
                val party = mp.partyName?.split("/")?.lastOrNull()
                val district = mp.electoralDistrictName?.split("/")?.lastOrNull()
                if (party != null && district != null) "$name($party, $district)" else name
            } ?: name
        }
        val joinedMainProposers = enhancedMainProposers.joinToString(", ")

        // 4. 발의의원 전체 목록 추출
        val proposerSection = document.select("div.links.textType02 p:contains(· 발의의원 명단)").firstOrNull()
        val proposerElements = proposerSection?.parent()?.select("a[href]") ?: emptyList()
        val allProposers: List<String> = proposerElements.map { it.text().trim() }

        // 5. 공동 발의자: 전체 발의의원에서 대표 발의자(비교는 "이광희"와 같이 의원명이 깨끗하게 된 상태) 제거
        val normalizedMainProposers = mainProposerNames.map { it.trim() }
        val coProposers: List<String> = allProposers
            .map { it.replace(Regex("\\(.*\\)"), "").trim() }
            .filter { it.isNotEmpty() && it !in normalizedMainProposers }

        // 6. 찬성의원 전체 목록 추출
        val supporterSection = document.select("div.links.textType02 p:contains(· 찬성의원 명단)").firstOrNull()
        val supporterElements = supporterSection?.parent()?.select("a[href]") ?: emptyList()
        val allSupporters: List<String> = supporterElements.map { it.text().trim() }

        // 7. 찬성의원 처리:
        // 만약 대표 텍스트 내 괄호 부분에 "외"가 포함되어 있으면, 찬성의원 목록에서 대표 발의자와 겹치지 않는 이름만 사용.
        // 그렇지 않으면 빈 리스트로 처리.
        val supporters: List<String> = if ((capSep1 == "외" || capSep2 == "외")) {
            allSupporters.map { it.replace(Regex("\\(.*\\)"), "").trim() }
                .filter { it.isNotEmpty() && it !in normalizedMainProposers }
        } else {
            emptyList()
        }

        // 8. 인원수 재계산
        // "등"의 숫자는 대표발의자와 공동발의자 전체 수.
        val computedEtc = mainProposerNames.size + coProposers.size
        // "외"의 숫자는 찬성의원의 전체 수.
        val computedExtra = supporters.size

        // 9. 최종 발의요약(overview) 조립 (순서: 대표발의자 보강정보 → (있는 경우) "등 X인" → (있는 경우) "외 Y인")
        val overviewParts = mutableListOf<String>()
        if (joinedMainProposers.isNotBlank()) overviewParts.add(joinedMainProposers)
        if (computedEtc > mainProposerNames.size) { // 공동발의자가 있는 경우
            overviewParts.add("등 ${computedEtc}인")
        }
        if (computedExtra > 0) {
            overviewParts.add("외 ${computedExtra}인")
        }
        val proposerOverview = overviewParts.joinToString(" ")

        logger.info("발의 요약: $proposerOverview")
        logger.info("대표 발의자: 인원수: ${mainProposerNames.size} $mainProposerNames")
        logger.info("공동 발의자: 인원수: ${coProposers.size} $coProposers")
        logger.info("찬성 의원: 인원수: ${supporters.size} $supporters")

        return ParticipantSyncDataModel(
            proposerOverview = proposerOverview,
            mainProposers = mainProposerNames,
            coProposers = coProposers,
            supporters = supporters
        )
    }

    @Serializable
    data class LawProposalPreviewText(
        @SerialName("title")
        val titleType: PreviewTextTitleType,
        @SerialName("translatedTitle")
        val translatedTitle: String? = null,
        @SerialName("contents")
        val contents: List<String>
    )

    enum class PreviewTextTitleType(val desc: String) {

        REASON_AND_MAIN_CONTENTS("제안이유 및 주요내용"),
        REASON("제안이유"),
        MAIN_CONTENTS("주요내용"),
        REFERENCE("참고사항");

        companion object {

            private val logger: Logger = LoggerFactory.getLogger(this::class.java)

            fun matchTitleType(title: String): PreviewTextTitleType? {
                val cleanedTitle = title.replace("\\s+".toRegex(), "").trim() // 공백 제거
                val matchedType = entries
                    .sortedByDescending { it.desc.count() } // 긴 제목부터 비교
                    .find { cleanedTitle.contains(it.desc.replace("\\s+".toRegex(), "").trim()) }

                // 🔥 디버깅 로그 추가
                if (matchedType != null) {
                    logger.info("✅ 제목 매칭됨: '$title' -> '${matchedType.desc}'")
                }

                return matchedType
            }

            fun PreviewTextTitleType.getTranslatedTitle(languageCode: String): String {
                return TITLE_TRANSLATIONS[languageCode]?.get(this)
                    ?: throw IllegalArgumentException("언어코드에 맞는 제목 타입이 없음")
            }

            // 🔥 언어별 번역된 제목을 저장하는 상수
            private val TITLE_TRANSLATIONS: Map<String, Map<PreviewTextTitleType, String>> = mapOf(
                "en" to mapOf(
                    REASON to "Reason",
                    MAIN_CONTENTS to "Main Contents",
                    REASON_AND_MAIN_CONTENTS to "Reason & Main Contents",
                    REFERENCE to "Reference"
                ),
                "zh" to mapOf(
                    REASON to "提案理由",
                    MAIN_CONTENTS to "主要内容",
                    REASON_AND_MAIN_CONTENTS to "提案理由及主要内容",
                    REFERENCE to "参考事项"
                ),
                "ja" to mapOf(
                    REASON to "提案の理由",
                    MAIN_CONTENTS to "主要内容",
                    REASON_AND_MAIN_CONTENTS to "提案理由と主要内容",
                    REFERENCE to "参考事項"
                )
            )

            // 🔥 언어별 조항 변환 저장
            val PREVIEW_CONTENT_CLAUSE_TRANSLATIONS: Map<String, Map<String, String>> = mapOf(
                "en" to mapOf(
                    "가." to "A.", "나." to "B.", "다." to "C.", "라." to "D.",
                    "마." to "E.", "바." to "F.", "사." to "G.", "아." to "H.",
                    "자." to "I.", "차." to "J.", "카." to "K.", "타." to "L.",
                    "파." to "M.", "하." to "N."
                ),
                "zh" to mapOf(
                    "가." to "甲.", "나." to "乙.", "다." to "丙.", "라." to "丁.",
                    "마." to "戊.", "바." to "己.", "사." to "庚.", "아." to "辛.",
                    "자." to "壬.", "차." to "癸.", "카." to "子.", "타." to "丑.",
                    "파." to "寅.", "하." to "卯."
                ),
                "ja" to mapOf(
                    "가." to "ア.", "나." to "イ.", "다." to "ウ.", "라." to "エ.",
                    "마." to "オ.", "바." to "カ.", "사." to "キ.", "아." to "ク.",
                    "자." to "ケ.", "차." to "コ.", "카." to "サ.", "타." to "シ.",
                    "파." to "ス.", "하." to "セ."
                )
            )
        }
    }
}