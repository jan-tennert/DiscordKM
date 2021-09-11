package com.github.jan.discordkm.events.internal

import com.github.jan.discordkm.Client
import com.github.jan.discordkm.entities.guild.Guild
import com.github.jan.discordkm.events.GuildCreateEvent
import com.github.jan.discordkm.utils.extractClientEntity
import kotlinx.serialization.json.JsonObject

internal class GuildCreateEventHandler(val client: Client) : InternalEventHandler<GuildCreateEvent> {

    override fun handle(data: JsonObject): GuildCreateEvent {
        val guild = data.extractClientEntity<Guild>(client)
        client.guildCache[guild.id] = guild
        return GuildCreateEvent(guild, client)
    }

}