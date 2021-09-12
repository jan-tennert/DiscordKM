package io.github.jan.discordkm.entities.guild

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.SerializableEntity

interface GuildEntity : SerializableEntity  {

    val guild: Guild
    override val client: Client
        get() = guild.client

}