/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild.channels

import io.github.jan.discordkm.api.entities.guild.channels.modifier.GuildChannelBuilder
import io.github.jan.discordkm.api.entities.guild.channels.modifier.TextChannelModifier
import io.github.jan.discordkm.api.entities.lists.retrieve
import io.github.jan.discordkm.internal.entities.guilds.channels.NewsChannelData

/**
 * A text channel is a [GuildChannel] where you can send messages
 */
interface TextChannel : GuildTextChannel {

    companion object : GuildChannelBuilder<GuildTextChannel, TextChannelModifier> {

        override fun create(builder: TextChannelModifier.() -> Unit) = TextChannelModifier(0).apply(builder).build()

    }

    /**
     * Creates a thread in this channel without a message
     * @param name The name this thread will get
     * @param autoArchiveDuration The duration after the thread will be achieved
     * @param invitable Whether if non-moderators can add non-moderators to this private thread
     * @see Thread.ThreadDuration
    */
    suspend fun createPrivateThread(name: String, autoArchiveDuration: Thread.ThreadDuration = defaultAutoArchiveDuration, invitable: Boolean? = null): Thread

    /**
     * Converts this text channel to a news channel
     */
    fun asNewsChannel(): NewsChannel = NewsChannelData(guild, data)

    override suspend fun retrieve() = guild.channels.retrieve(id) as TextChannel

}