package com.nana.nana.domain.feature.mp.loader

import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.toDecodedUriString
import org.springframework.web.util.UriComponentsBuilder

/**
 * BTH_DATE 반드시 2번
 * null 안되고 반드시 빈 문자열
 */
fun downloadAndGetCurrentMPJsonPath(): Pair<String, String> {
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

    val downloadFilePath = "src/main/resources/json/mp/current/mp_current_22.json"
    return url to downloadFilePath
}

/**
 * BTH_DATE 반드시 2번
 * null 안되고 반드시 빈 문자열
 */
fun downloadAndGetCurrentMPSNSJsonPath(): Pair<String, String> {
    val downloadBaseUrl = "https://open.assembly.go.kr/portal/data/sheet/downloadSheetData.do"

    val url = UriComponentsBuilder.fromUriString(downloadBaseUrl)
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

    val downloadFilePath = "src/main/resources/json/mp/sns/mp_current_sns.json"
    return url to downloadFilePath
}