/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.channels.guild

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.cacheManager
import io.github.jan.discordkm.api.entities.modifier.guild.GuildChannelBuilder
import io.github.jan.discordkm.api.entities.modifier.guild.TextChannelModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


sealed interface NewsChannel: GuildTextChannel {

    override val type: ChannelType
        get() = ChannelType.GUILD_NEWS
    override val cache: NewsChannelCacheEntry?
        get() = guild.cache?.cacheManager?.channelCache?.get(id) as? NewsChannelCacheEntry

    /*
     * Follows the news channel which means messages which are sent in this channel can be published to send the message to every channel which is following this channel.
     * @param targetId The id of the channel where the messages are going to be sent when a new message is published
     */
    suspend fun follow(targetId: Snowflake) = client.buildRestAction<Unit> {
        route = Route.Channel.FOLLOW_CHANNEL(id).post(buildJsonObject {
            put("webhook_channel_id", targetId.long)
        })
    }

    suspend fun follow(target: GuildTextChannel) = follow(target.id)

    companion object : GuildChannelBuilder<TextChannelModifier, NewsChannel> {
        override fun createChannel(modifier: TextChannelModifier.() -> Unit) = TextChannelModifier().apply { convertToNewsChannel(); modifier(this) }

        operator fun invoke(id: Snowflake, guild: Guild): NewsChannel = NewsChannelImpl(id, guild)
        operator fun invoke(data: JsonObject, guild: Guild) = ChannelSerializer.deserializeChannel<NewsChannelCacheEntry>(data, guild)
    }

}

internal class NewsChannelImpl(override val id: Snowflake, override val guild: Guild) : NewsChannel {

    override fun toString(): String = "NewsChannel(id=$id, type=$type)"
    override fun equals(other: Any?): Boolean = other is NewsChannel && other.id == id && other.guild.id == guild.id
    override fun hashCode(): Int = id.hashCode()

}