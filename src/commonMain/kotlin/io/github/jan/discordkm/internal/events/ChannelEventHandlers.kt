/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

import com.soywiz.klock.ISO8601
import io.github.jan.discordkm.api.entities.channels.Channel
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.channels.guild.GuildMessageChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.GuildTextChannel
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.cacheManager
import io.github.jan.discordkm.api.events.ChannelCreateEvent
import io.github.jan.discordkm.api.events.ChannelDeleteEvent
import io.github.jan.discordkm.api.events.ChannelPinUpdateEvent
import io.github.jan.discordkm.api.events.ChannelUpdateEvent
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.snowflake
import kotlinx.serialization.json.JsonObject

internal class ChannelCreateEventHandler(val client: DiscordClient) :
    InternalEventHandler<ChannelCreateEvent> {

    override suspend fun handle(data: JsonObject): ChannelCreateEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val channel = ChannelSerializer.deserialize(data, guild)
        guild.cache?.cacheManager?.channelCache?.set(channel.id, channel)
        return ChannelCreateEvent(channel)
    }

}

internal class ChannelDeleteEventHandler(val client: DiscordClient) :
    InternalEventHandler<ChannelDeleteEvent> {

    override suspend fun handle(data: JsonObject): ChannelDeleteEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val channel = ChannelSerializer.deserialize(data, guild)
        guild.cache?.cacheManager?.channelCache?.remove(channel.id)
        return ChannelDeleteEvent(channel)
    }

}

internal class ChannelUpdateEventHandler(val client: DiscordClient) :
    InternalEventHandler<ChannelUpdateEvent> {

    override suspend fun handle(data: JsonObject): ChannelUpdateEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val channel = ChannelSerializer.deserialize(data, guild)
        val oldChannel = guild.cache?.cacheManager?.channelCache?.get(channel.id)
        if(oldChannel != null && oldChannel is GuildMessageChannelCacheEntry && channel is GuildMessageChannelCacheEntry) {
            oldChannel.cacheManager.fillCache(channel.cacheManager)
        }
        guild.cache?.cacheManager?.channelCache?.set(channel.id, channel)
        return ChannelUpdateEvent(channel, oldChannel)
    }

}

internal class ChannelPinUpdateEventHandler(val client: DiscordClient) :
    InternalEventHandler<ChannelPinUpdateEvent> {

    override suspend fun handle(data: JsonObject): ChannelPinUpdateEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val channel = Channel(data["channel_id"]!!.snowflake, ChannelType.UNKNOWN, client, guild) as GuildTextChannel
        val lastPinTimestamp = ISO8601.DATETIME_UTC_COMPLETE.tryParse(data.getOrNull<String>("last_pin_timestamp") ?: "")
        return ChannelPinUpdateEvent(channel, lastPinTimestamp)
    }

}