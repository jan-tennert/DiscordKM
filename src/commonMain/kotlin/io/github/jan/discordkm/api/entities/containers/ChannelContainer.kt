/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.channels.guild.GuildChannel
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannelCacheEntry
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.GuildEntity
import io.github.jan.discordkm.api.entities.modifier.guild.GuildChannelBuilder
import io.github.jan.discordkm.api.entities.modifier.guild.GuildChannelModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.jsonObject

open class GuildChannelContainer(override val guild: Guild) : GuildEntity {

    /*
     * Returns all guild channels in this guild
     */
    suspend fun retrieveChannels() = guild.client.buildRestAction<List<GuildChannel>> {
        route = Route.Channel.GET_CHANNELS(guild.id).get()
        transform { it.toJsonArray().map { json -> ChannelSerializer.deserialize(json.jsonObject, guild) } }
    }

    /*
     * Creates a guild channel
     * @param type The type like [VoiceChannel], [TextChannel] etc.
     */
    suspend inline fun <reified C: GuildChannel, M: GuildChannelModifier, T : GuildChannelBuilder<M, C>> create(type: T, reason: String? = null, noinline builder: M.() -> Unit) : C = guild.client.buildRestAction<C> {
        route = Route.Channel.CREATE_CHANNEL(guild.id).post(type.createChannel(builder).data)
        transform { ChannelSerializer.deserializeChannel(it.toJsonObject(), guild) as C }
        this.reason = reason
    }

}

class CacheGuildChannelContainer(guild: Guild, override val values: Collection<GuildChannelCacheEntry>) : GuildChannelContainer(guild), NameableSnowflakeContainer<GuildChannelCacheEntry>
class CacheChannelContainer(override val values: Collection<GuildChannelCacheEntry>) : NameableSnowflakeContainer<GuildChannelCacheEntry>