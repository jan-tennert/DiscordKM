/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.guild.channels

import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.entities.guild.channels.modifier.GuildChannelBuilder
import io.github.jan.discordkm.entities.guild.channels.modifier.TextChannelModifier
import io.github.jan.discordkm.restaction.CallsTheAPI
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractGuildEntity
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class NewsChannel(guild: Guild, data: JsonObject) : GuildTextChannel(guild, data) {

    /**
     * Modifies this news channel
     * A news channel can also be converted to an [TextChannel] by setting the type parameter [T] to TextChannel
     */
    @CallsTheAPI
    suspend inline fun <reified T : GuildTextChannel> modify(modifier: TextChannelModifier.() -> Unit = {}): T = client.buildRestAction {
        val type = when(T::class) {
            TextChannel::class -> 0
            NewsChannel::class -> null
            else -> throw IllegalStateException()
        }
        action = RestAction.Action.patch("/channels/$id", TextChannelModifier(type).apply(modifier).build())
        transform {
            when(type) {
                0 -> it.toJsonObject().extractGuildEntity<TextChannel>(guild) as T
                null -> it.toJsonObject().extractGuildEntity<NewsChannel>(guild) as T
                else -> throw IllegalStateException()
            }
        }
        onFinish { guild.channelCache[id] = it }
    }

    /**
     * Follows this [NewsChannel], which means members of this guild can publish a message in this channel and the channel id with [targetId] will also get the message
     */
    suspend fun follow(targetId: Snowflake) = client.buildRestAction<Unit> {
        action = RestAction.Action.post("/channels/$id/followers", buildJsonObject {
            put("webhook_channel_id", targetId.long)
        })
        transform {}
        //check permission
    }

    companion object : GuildChannelBuilder<GuildTextChannel, TextChannelModifier> {

        override fun create(builder: TextChannelModifier.() -> Unit) = TextChannelModifier(5).apply(builder).build()

    }

    fun asTextChannel() = TextChannel(guild, data)

}