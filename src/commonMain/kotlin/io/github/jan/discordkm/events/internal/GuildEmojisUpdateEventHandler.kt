package io.github.jan.discordkm.events.internal

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.guild.Emoji
import io.github.jan.discordkm.events.GuildEmojiUpdateEvent
import io.github.jan.discordkm.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class GuildEmojiUpdateEventHandler(val client: Client) : InternalEventHandler<GuildEmojiUpdateEvent> {

    override fun handle(data: JsonObject): GuildEmojiUpdateEvent {
        val emotes = data.getValue("emojis").jsonArray.map { Emoji.Emote(it.jsonObject, client) }
        val guildId = data.getOrThrow<Snowflake>("guild_id")
        return GuildEmojiUpdateEvent(client, guildId, emotes)
    }

}