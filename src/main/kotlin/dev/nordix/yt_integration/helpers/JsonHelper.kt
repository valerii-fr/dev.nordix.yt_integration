package dev.nordix.yt_integration.helpers

import kotlinx.serialization.json.Json

object JsonHelper {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
        encodeDefaults = true
        classDiscriminator = "#class"
    }
}