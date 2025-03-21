package com.nana.nana.domain.feature.assemblysync.mp.apiclient

import com.nana.nana.config.ApiConfig.defaultJson
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.toDecodedUriString
import com.nana.nana.domain.feature.assemblysync.mp.apiclient.json.SyncMPJsonProperty
import com.nana.nana.domain.feature.assemblysync.mp.apiclient.json.SyncMPSnsJsonProperty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriComponentsBuilder
import java.io.File
import java.net.URI

/**
 * BTH_DATE 반드시 2번
 * null 안되고 반드시 빈 문자열
 */
object MPsJsonDownloader {

    private val logger: Logger = LoggerFactory.getLogger(MPsJsonDownloader::class.java)

    fun getCurrentMPsJson(): List<SyncMPJsonProperty> {
        val downloadBaseUrl = "https://open.assembly.go.kr/portal/data/sheet/downloadSheetData.do"

        val url = UriComponentsBuilder.fromUriString(downloadBaseUrl)
            .queryParam("selCnt", "300")
            .queryParam("rows", "100")
            .queryParam("infId", "OWSSC6001134T516707")
            .queryParam("infSeq", "1")
            .queryParam("downloadType", "J")
            .queryParam("loc", "")
            .queryParam("schChkInfaId", "")
            .queryParam("HG_NM", "")
            .queryParam("BTH_GBN_NM", "")
            .queryParam("BTH_DATE", "")
            .queryParam("BTH_DATE", "")
            .queryParam("POLY_NM", "")
            .queryParam("ORIG_NM", "")
            .queryParam("CMIT_NM", "")
            .queryParam("REELE_GBN_NM", "")
            .queryParam("UNITS", "")
            .queryParam("SEX_GBN_NM", "")
            .queryParam("STAFF", "")
            .queryParam("SECRETARY", "")
            .queryParam("SECRETARY2", "")
            .toDecodedUriString()

        val filePath = "src/main/resources/json/mp/current/mp_current_22.json"

        URI(url).toURL().openStream().use { input ->
            File(filePath).outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val file = File(filePath)
        if (!file.exists()) {
            logger.error("현 국회의원 리스트 다운로드 실패: $filePath")
            return emptyList()
        }

        val jsonString = file.readText()
        val jsonMPs = defaultJson.decodeFromString<List<SyncMPJsonProperty>>(jsonString)
            .drop(1)

        logger.info("현 국회의원 리스트 다운로드 성공: $filePath")
        return jsonMPs
    }

    /**
     * BTH_DATE 반드시 2번
     * null 안되고 반드시 빈 문자열
     */
    fun getCurrentMPsSnsJson(): List<SyncMPSnsJsonProperty> {
        val downloadBaseUrl = "https://open.assembly.go.kr/portal/data/sheet/downloadSheetData.do"

        val snsUrl = UriComponentsBuilder.fromUriString(downloadBaseUrl)
            .queryParam("selCnt", "300")
            .queryParam("rows", "100")
            .queryParam("infId", "OET0D9001078G318850")
            .queryParam("infSeq", "1")
            .queryParam("downloadType", "J")
            .queryParam("loc", "")
            .queryParam("schChkInfaId", "")
            .queryParam("HG_NM", "")
            .queryParam("BTH_GBN_NM", "")
            .queryParam("BTH_DATE", "")
            .queryParam("BTH_DATE", "")
            .queryParam("POLY_NM", "")
            .queryParam("ORIG_NM", "")
            .queryParam("CMIT_NM", "")
            .queryParam("REELE_GBN_NM", "")
            .queryParam("UNITS", "")
            .queryParam("SEX_GBN_NM", "")
            .queryParam("STAFF", "")
            .queryParam("SECRETARY", "")
            .queryParam("SECRETARY2", "")
            .toDecodedUriString()

        val filePath = "src/main/resources/json/mp/sns/mp_current_sns.json"

        URI(snsUrl).toURL().openStream().use { input ->
            File(filePath).outputStream().use { output ->
                input.copyTo(output)
            }
        }
        val snsFile = File(filePath)
        if (!snsFile.exists()) {
            logger.error("현 국회의원 SNS 리스트 다운로드 실패: $filePath")
            return emptyList()
        }

        val jsonSnsString = snsFile.readText()
        val jsonMPSNS = defaultJson.decodeFromString<List<SyncMPSnsJsonProperty>>(jsonSnsString)
            .drop(1)

        return jsonMPSNS
    }
}