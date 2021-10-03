package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.channels.GuildMessageChannel
import io.github.jan.discordkm.api.events.WebhooksUpdateEvent
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject

class WebhooksUpdateEventHandler(val client: Client) : InternalEventHandler<WebhooksUpdateEvent> {

    override fun handle(data: JsonObject): WebhooksUpdateEvent {
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")]!!
        val channel = client.channels[data.getOrThrow<Snowflake>("channel_id")]!! as GuildMessageChannel
        return WebhooksUpdateEvent(guild, channel)
    }

}