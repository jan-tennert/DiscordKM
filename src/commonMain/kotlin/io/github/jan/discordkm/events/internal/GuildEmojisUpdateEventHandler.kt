package io.github.jan.discordkm.events.internal

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.guild.Emoji
import io.github.jan.discordkm.events.GuildEmojisUpdateEvent
import io.github.jan.discordkm.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class GuildEmojisUpdateEventHandler(val client: Client) : InternalEventHandler<GuildEmojisUpdateEvent> {

    override fun handle(data: JsonObject): GuildEmojisUpdateEvent {
        val emotes = data.getValue("emojis").jsonArray.map { Emoji.Emote(it.jsonObject, client) }
        val guildId = data.getOrThrow<Snowflake>("guild_id")
        return GuildEmojisUpdateEvent(client, guildId, emotes)
    }

}