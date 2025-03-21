package com.nana.nana.domain.data

import java.io.File

object ResourcePathConverter {

    private const val BASE_RES_PATH = "src/main/resources"
    private const val BASE_JSON_PATH = "$BASE_RES_PATH/json"

    fun getFileFromMPJsonPath(erc: Int): File {
        val fileName = "mp_kr_$erc.json"
        return File("$BASE_JSON_PATH/mp/past/$fileName")
    }

    fun getFileFromPresidentJsonPath(): File {
        val fileName = "presidents_kr.json"
        return File("$BASE_JSON_PATH/president/$fileName")
    }
}