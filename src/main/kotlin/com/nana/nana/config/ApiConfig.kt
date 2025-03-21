package com.nana.nana.config

import kotlinx.serialization.json.Json

object ApiConfig {

    val defaultJson = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
    }
}