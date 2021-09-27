package io.github.jan.discordkm.internal.events.internal

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.events.GuildEmojisUpdateEvent
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class GuildEmojisUpdateEventHandler(val client: Client) : InternalEventHandler<GuildEmojisUpdateEvent> {

    override fun handle(data: JsonObject): GuildEmojisUpdateEvent {
        val emotes = data.getValue("emojis").jsonArray.map { Emoji.Emote(it.jsonObject, client) }
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")]!!
        return GuildEmojisUpdateEvent(guild, emotes)
    }

}