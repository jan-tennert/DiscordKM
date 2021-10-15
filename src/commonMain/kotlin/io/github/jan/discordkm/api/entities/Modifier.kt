package io.github.jan.discordkm.api.entities

import kotlinx.serialization.json.JsonObject

interface Modifier {

    fun build() : JsonObject

}