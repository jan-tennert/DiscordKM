/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.lists

import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.entities.guild.channels.Category
import io.github.jan.discordkm.entities.guild.channels.GuildChannel
import io.github.jan.discordkm.entities.guild.channels.NewsChannel
import io.github.jan.discordkm.entities.guild.channels.StageChannel
import io.github.jan.discordkm.entities.guild.channels.TextChannel
import io.github.jan.discordkm.entities.guild.channels.VoiceChannel
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractGuildEntity
import io.github.jan.discordkm.utils.toJsonObject

sealed interface IChannelList : DiscordList<GuildChannel> {

    override fun get(name: String) = internalList.filter { it.name == name }

}

class RetrievableChannelList(val guild: Guild, override val internalList: List<GuildChannel>) : IChannelList {

    suspend inline fun <reified T : GuildChannel> retrieve(id: Snowflake) = guild.client.buildRestAction<T> {
        action = RestAction.Action.get("/channels/$id")
        transform { it.toJsonObject().extractGuildEntity(guild) }
        onFinish { guild.channelCache[id] = it }
    }

}

class ChannelList(override val internalList: List<GuildChannel>) : IChannelList

inline fun <reified C : GuildChannel> IChannelList.getGuildChannel(id: Snowflake) = when(C::class) {
    VoiceChannel::class -> get(id) as C
    StageChannel::class -> get(id) as C
    TextChannel::class -> get(id) as C
    Category::class -> get(id) as C
    NewsChannel::class -> get(id) as C
    else -> throw IllegalStateException()
}