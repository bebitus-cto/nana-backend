package com.nana.nana.domain.feature.assemblysync.pr.apiclient

import com.nana.nana.config.ApiConfig.defaultJson
import com.nana.nana.domain.feature.assemblysync.mp.apiclient.json.SyncResourcePathConverter.BASE_JSON_PATH
import com.nana.nana.domain.feature.assemblysync.pr.response.PresidentApiResponse
import com.nana.nana.domain.feature.assemblysync.pr.response.PrResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

object AllPrsJsonDownloader {

    private val logger: Logger = LoggerFactory.getLogger(AllPrsJsonDownloader::class.java)

    fun getAllPrsJson(): List<PrResponse> {

        val fileName = "presidents_kr.json"
        val file = File("$BASE_JSON_PATH/president/$fileName")

        if (!file.exists()) {
            logger.error("역대 및 현 대통령 파일이 없음: ${file.path}")
            return emptyList()
        }

        val jsonString = file.readText()
        val response = defaultJson.decodeFromString<PresidentApiResponse>(jsonString)
        val presidentResponse = response.presidents

        return if (presidentResponse.isNullOrEmpty()) {
            emptyList()
        } else {
            presidentResponse
        }
    }
}