package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.events.GuildEmojisUpdateEvent
import io.github.jan.discordkm.internal.Cache
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class GuildEmojisUpdateEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<GuildEmojisUpdateEvent> {

    override fun handle(data: JsonObject): GuildEmojisUpdateEvent {
        val emotes = data.getValue("emojis").jsonArray.map { Emoji.Emote(it.jsonObject, client) }
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")]!!
        if(Cache.EMOJIS in client.enabledCache) {
            val cache = (guild as GuildData).emojiCache
            cache.internalMap.clear()
            emotes.forEach { cache[it.id] = it }
        }
        return GuildEmojisUpdateEvent(guild, emotes)
    }

}