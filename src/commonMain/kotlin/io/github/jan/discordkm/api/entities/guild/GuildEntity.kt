package io.github.jan.discordkm.api.entities.guild

import io.github.jan.discordkm.api.entities.SerializableEntity
import io.github.jan.discordkm.api.entities.clients.Client

interface GuildEntity : SerializableEntity  {

    val guild: Guild
    override val client: Client
        get() = guild.client

}