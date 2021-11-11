package io.github.jan.discordkm.internal.serialization

import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import kotlinx.serialization.json.JsonObject

sealed interface CustomEntitySerializer <K, T : BaseEntity> {

    fun deserialize(data: JsonObject, value: K) : T

}

interface BaseEntitySerializer <T : BaseEntity> : CustomEntitySerializer<Client, T>
interface GuildEntitySerializer <T : BaseEntity> : CustomEntitySerializer<Guild, T>
