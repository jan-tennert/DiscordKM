package io.github.jan.discordkm.api.entities.modifiers

import kotlinx.serialization.json.JsonObject

interface BaseModifier {

    val data: JsonObject

}