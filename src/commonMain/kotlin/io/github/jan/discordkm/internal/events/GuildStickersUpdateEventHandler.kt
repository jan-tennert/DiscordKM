package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.guild.Sticker
import io.github.jan.discordkm.api.events.GuildStickersUpdateEvent
import io.github.jan.discordkm.internal.Cache
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray

class GuildStickersUpdateEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<GuildStickersUpdateEvent> {

    override fun handle(data: JsonObject): GuildStickersUpdateEvent {
        val stickers = data.getValue("stickers").jsonArray.map { Sticker(data, client) }
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")]!!
        if(Cache.STICKERS in client.enabledCache) {
            val cache = (guild as GuildData).stickerCache
            cache.internalMap.clear()
            stickers.forEach { cache[it.id] = it }
        }
        return GuildStickersUpdateEvent(guild, stickers)
    }

}