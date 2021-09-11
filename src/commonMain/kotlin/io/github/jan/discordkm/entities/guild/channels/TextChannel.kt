/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.guild.channels

import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.entities.guild.channels.modifier.TextChannelModifier
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractGuildEntity
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.json.JsonObject

class TextChannel(guild: Guild, data: JsonObject) : GuildTextChannel(guild, data) {

    suspend inline fun <reified T : GuildTextChannel> modify(modifier: TextChannelModifier.() -> Unit = {}): T = client.buildRestAction {
        val type = when(T::class) {
            TextChannel::class -> null
            NewsChannel::class -> 5
            else -> throw IllegalStateException()
        }
        action = RestAction.Action.patch("/channels/$id", TextChannelModifier(type).apply(modifier).build())
        transform {
            when(type) {
                null -> it.toJsonObject().extractGuildEntity<TextChannel>(guild) as T
                5 -> it.toJsonObject().extractGuildEntity<NewsChannel>(guild) as T
                else -> throw IllegalStateException()
            }
        }
        onFinish { guild.channelCache[id] = it }
    }

    fun asNewsChannel() = NewsChannel(guild, data)

}