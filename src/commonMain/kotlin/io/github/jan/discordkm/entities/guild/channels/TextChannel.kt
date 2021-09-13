/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.guild.channels

import io.github.jan.discordkm.entities.channels.ChannelType
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

class TextChannel(guild: Guild, data: JsonObject) : GuildTextChannel(guild, data) {

    companion object : GuildChannelBuilder<GuildTextChannel, TextChannelModifier> {

        override fun create(builder: TextChannelModifier.() -> Unit) = TextChannelModifier(0).apply(builder).build()

    }

    /**
     * Modifies this news channel
     * A news channel can also be converted to an [NewsChannel] by setting the type parameter [T] to NewsChannel
     */
    @CallsTheAPI
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

    /**
     * Creates a thread in this channel without a message
     * @param name The name this thread will get
     * @param autoArchiveDuration The duration after the thread will be achieved
     * @param invitable Whether if non-moderators can add non-moderators to this private thread
    */
    @CallsTheAPI
    suspend fun createPrivateThread(name: String, autoArchiveDuration: Thread.ThreadDuration = defaultAutoArchiveDuration, invitable: Boolean? = null) = client.buildRestAction<Thread> {
        action = RestAction.Action.post("/channels/$id/threads", buildJsonObject {
            put("name", name)
            put("auto_archive_duration", autoArchiveDuration.duration.minutes.toInt())
            put("type", ChannelType.GUILD_PRIVATE_THREAD.id)
            put("invitable", invitable)
        })
        transform { it.toJsonObject().extractGuildEntity(guild) }
        onFinish { guild.threadCache[it.id] = it }
        //check permission
    }

    fun asNewsChannel() = NewsChannel(guild, data)

}