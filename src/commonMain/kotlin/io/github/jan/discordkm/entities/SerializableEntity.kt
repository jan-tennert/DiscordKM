package io.github.jan.discordkm.entities

import kotlinx.serialization.json.JsonObject

interface SerializableEntity : BaseEntity {

    val data: JsonObject

}