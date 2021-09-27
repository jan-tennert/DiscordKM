package io.github.jan.discordkm.internal.events.internal

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.channels.GuildChannel
import io.github.jan.discordkm.api.events.ChannelCreateEvent
import io.github.jan.discordkm.api.events.ChannelDeleteEvent
import io.github.jan.discordkm.api.events.ChannelUpdateEvent
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.utils.extractChannel
import io.github.jan.discordkm.internal.utils.getOrNull
import kotlinx.serialization.json.JsonObject

class ChannelCreateEventHandler(val client: Client) : InternalEventHandler<ChannelCreateEvent> {

    override fun handle(data: JsonObject): ChannelCreateEvent {
        val guild = client.guilds[data.getOrNull<Snowflake>("guild_id") ?: Snowflake.empty()]
        val channel = data.extractChannel(client, guild) as GuildChannel
        (guild as? GuildData)?.channelCache?.set(channel.id, channel)
        return ChannelCreateEvent(channel)
    }

}

class ChannelDeleteEventHandler(val client: Client) : InternalEventHandler<ChannelDeleteEvent> {

    override fun handle(data: JsonObject): ChannelDeleteEvent {
        val guild = client.guilds[data.getOrNull<Snowflake>("guild_id") ?: Snowflake.empty()]
        val channel = data.extractChannel(client, guild) as GuildChannel
        (guild as? GuildData)?.channelCache?.remove(channel.id)
        return ChannelDeleteEvent(channel)
    }

}

class ChannelUpdateEventHandler(val client: Client) : InternalEventHandler<ChannelUpdateEvent> {

    override fun handle(data: JsonObject): ChannelUpdateEvent {
        val guild = client.guilds[data.getOrNull<Snowflake>("guild_id") ?: Snowflake.empty()]
        val channel = data.extractChannel(client, guild) as GuildChannel
        (guild as? GuildData)?.channelCache?.set(channel.id, channel)
        return ChannelUpdateEvent(channel)
    }

}