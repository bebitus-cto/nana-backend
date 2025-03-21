package com.nana.nana.domain.feature.assemblysync.mp.apiclient.json

import com.nana.nana.config.ApiConfig.defaultJson
import com.nana.nana.domain.feature.assemblysync.config.SyncConfig.CURRENT_MP_ERACO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

object SyncResourcePathConverter {

    private const val BASE_RES_PATH = "src/main/resources"
    const val BASE_JSON_PATH = "$BASE_RES_PATH/json"

    private val logger: Logger = LoggerFactory.getLogger(SyncResourcePathConverter::class.java)

    fun getFileFromPastMPsJsonPath(): List<SyncMPJsonProperty> {

        val pastMPs = mutableListOf<SyncMPJsonProperty>()

        for (eraco in 1 until CURRENT_MP_ERACO) {
            val fileName = "mp_kr_$eraco.json"
            val file = File("$BASE_JSON_PATH/mp/past/$fileName")
            if (!file.exists()) {
                logger.warn("🚨 [역대 국회의원] 파일 없음: ${file.path}")
                continue
            }

            val jsonString = file.readText()
            val jsonMPList = defaultJson.decodeFromString<List<SyncMPJsonProperty>>(jsonString).drop(1)
            pastMPs.addAll(jsonMPList)
        }

        if (pastMPs.isEmpty()) {
            logger.warn("🚨 [역대 국회의원] 동기화할 데이터가 없음")
            return emptyList()
        }
        return pastMPs
    }
}