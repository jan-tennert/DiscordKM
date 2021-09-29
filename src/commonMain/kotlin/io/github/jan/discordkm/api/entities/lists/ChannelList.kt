/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.lists

import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.channels.Category
import io.github.jan.discordkm.api.entities.guild.channels.GuildChannel
import io.github.jan.discordkm.api.entities.guild.channels.NewsChannel
import io.github.jan.discordkm.api.entities.guild.channels.StageChannel
import io.github.jan.discordkm.api.entities.guild.channels.TextChannel
import io.github.jan.discordkm.api.entities.guild.channels.VoiceChannel
import io.github.jan.discordkm.api.entities.guild.channels.modifier.GuildChannelBuilder
import io.github.jan.discordkm.api.entities.guild.channels.modifier.GuildChannelModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.entities.channels.MessageChannel
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractChannel
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.jsonObject

sealed interface IChannelList : NameableSnowflakeList<GuildChannel>, BaseEntity

/**
 * Retrieves a guild channel by its id
 */
suspend inline fun IChannelList.retrieve(id: Snowflake) = client.buildRestAction<MessageChannel> {
    route = Route.Channel.GET_CHANNEL(id).get()
    transform { it.toJsonObject().extractChannel(client, client.guilds[it.toJsonObject().getOrNull<Snowflake>("guild_id") ?: Snowflake.empty()]) as MessageChannel }
    //onFinish { guild.channelCache[id] = it }
}

class RetrievableChannelList(val guild: Guild, override val internalMap: Map<Snowflake, GuildChannel>) : IChannelList {

    /**
     * Returns all guild channels in this guild
     */

    suspend fun retrieveChannels() = guild.client.buildRestAction<List<GuildChannel>> {
        route = Route.Channel.GET_CHANNELS(guild.id).get()
        transform { it.toJsonArray().map { json -> json.jsonObject.extractChannel(client, guild) as GuildChannel } }
        onFinish {
            (guild as GuildData).channelCache.internalMap.clear()
            guild.channelCache.internalMap.putAll(it.associateBy { channel -> channel.id })
        }
    }

    /**
     * Creates a guild channel
     * @param type The type
     *
     **/

    suspend inline fun <reified C: GuildChannel, M: GuildChannelModifier<C>, T : GuildChannelBuilder<C, M>> create(type: T, noinline builder: M.() -> Unit) : C = guild.client.buildRestAction<C> {
        route = Route.Channel.CREATE_CHANNEL(guild.id).post(type.create(builder))
        transform { it.toJsonObject().extractChannel(client, guild) as C }
        onFinish { (guild as GuildData).channelCache[it.id] = it }
    }

    override val client = guild.client
}

class ChannelList(override val client: Client, override val internalMap: Map<Snowflake, GuildChannel>) : IChannelList

inline fun <reified C : GuildChannel> IChannelList.getGuildChannel(id: Snowflake) : C {
    return when(C::class) {
        VoiceChannel::class -> get(id) as C
        StageChannel::class -> get(id) as C
        TextChannel::class -> get(id) as C
        Category::class -> get(id) as C
        NewsChannel::class -> get(id) as C
        else -> throw IllegalStateException()
    }
}