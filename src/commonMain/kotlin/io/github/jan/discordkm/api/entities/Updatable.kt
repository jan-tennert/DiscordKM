package io.github.jan.discordkm.api.entities

import kotlinx.serialization.json.JsonObject

interface Updatable {

    fun update(data: JsonObject)

}