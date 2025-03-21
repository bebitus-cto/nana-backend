package com.nana.nana.domain.feature.auth.jwt

import com.nana.nana.config.ApiConfig.defaultJson
import io.jsonwebtoken.io.Serializer
import io.jsonwebtoken.io.Deserializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class KotlinxJwtSerializer : Serializer<Map<String, Any>>, Deserializer<MutableMap<String, *>> {
    override fun serialize(t: Map<String, Any>): ByteArray {
        return Json.encodeToString(t).toByteArray()
    }

    override fun deserialize(bytes: ByteArray): MutableMap<String, *> {
        return Json.decodeFromString<Map<String, Any>>(bytes.decodeToString()).toMutableMap()
    }
}