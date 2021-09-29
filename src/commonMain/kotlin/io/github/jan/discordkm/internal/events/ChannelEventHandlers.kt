package io.github.jan.discordkm.internal.events

import com.soywiz.klock.ISO8601
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.guild.channels.GuildChannel
import io.github.jan.discordkm.api.entities.guild.channels.GuildTextChannel
import io.github.jan.discordkm.api.events.ChannelCreateEvent
import io.github.jan.discordkm.api.events.ChannelDeleteEvent
import io.github.jan.discordkm.api.events.ChannelPinUpdateEvent
import io.github.jan.discordkm.api.events.ChannelUpdateEvent
import io.github.jan.discordkm.internal.Cache
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.utils.extractChannel
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject

class ChannelCreateEventHandler(val client: DiscordWebSocketClient) :
    InternalEventHandler<ChannelCreateEvent> {

    override fun handle(data: JsonObject): ChannelCreateEvent {
        val guild = client.guilds[data.getOrNull<Snowflake>("guild_id") ?: Snowflake.empty()]
        val channel = data.extractChannel(client, guild) as GuildChannel
        if(Cache.CHANNELS in client.enabledCache) (guild as? GuildData)?.channelCache?.set(channel.id, channel)
        return ChannelCreateEvent(channel)
    }

}

class ChannelDeleteEventHandler(val client: DiscordWebSocketClient) :
    InternalEventHandler<ChannelDeleteEvent> {

    override fun handle(data: JsonObject): ChannelDeleteEvent {
        val guild = client.guilds[data.getOrNull<Snowflake>("guild_id") ?: Snowflake.empty()]
        val channel = data.extractChannel(client, guild) as GuildChannel
        if(Cache.CHANNELS in client.enabledCache) (guild as? GuildData)?.channelCache?.remove(channel.id)
        return ChannelDeleteEvent(channel)
    }

}

class ChannelUpdateEventHandler(val client: DiscordWebSocketClient) :
    InternalEventHandler<ChannelUpdateEvent> {

    override fun handle(data: JsonObject): ChannelUpdateEvent {
        val guild = client.guilds[data.getOrNull<Snowflake>("guild_id") ?: Snowflake.empty()]
        val channel = data.extractChannel(client, guild) as GuildChannel
        val oldChannel = guild?.channels?.get(channel.id)
        if(Cache.CHANNELS in client.enabledCache) (guild as? GuildData)?.channelCache?.set(channel.id, channel)
        return ChannelUpdateEvent(channel, oldChannel)
    }

}

class ChannelPinUpdateEventHandler(val client: Client) :
    InternalEventHandler<ChannelPinUpdateEvent> {

    override fun handle(data: JsonObject): ChannelPinUpdateEvent {
        val channel = client.channels[data.getOrThrow<Snowflake>("channel_id")]!! as GuildTextChannel
        val lastPinTimestamp = ISO8601.DATETIME_UTC_COMPLETE.tryParse(data.getOrNull<String>("last_pin_timestamp") ?: "")
        return ChannelPinUpdateEvent(channel, lastPinTimestamp)
    }

}