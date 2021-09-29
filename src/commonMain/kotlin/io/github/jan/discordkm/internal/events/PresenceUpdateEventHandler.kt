package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.events.PresenceUpdateEvent
import io.github.jan.discordkm.internal.Cache
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject

class PresenceUpdateEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<PresenceUpdateEvent> {

    override fun handle(data: JsonObject): PresenceUpdateEvent {
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")]!!
        val presence = Guild.GuildPresence(guild, data)
        val oldPresence = guild.presences[presence.member.id]
        if(Cache.PRESENCES in client.enabledCache) (guild as GuildData).presenceCache[presence.member.id] = presence
        return PresenceUpdateEvent(presence, oldPresence)
    }

}