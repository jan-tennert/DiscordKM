/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild.channels

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.channels.modifier.GuildChannelBuilder
import io.github.jan.discordkm.api.entities.guild.channels.modifier.TextChannelModifier
import io.github.jan.discordkm.api.entities.lists.retrieve
import io.github.jan.discordkm.internal.entities.guilds.channels.TextChannelData

interface NewsChannel : GuildTextChannel {

    /**
     * Follows this [NewsChannel], which means members of this guild can publish a message in this channel and the channel id with [targetId] will also get the message
     */
    suspend fun follow(targetId: Snowflake)

    /**
     * Follows this [NewsChannel], which means members of this guild can publish a message in this channel and the channel [channel] will also get the message
     */
    suspend fun follow(channel: TextChannel) = follow(channel.id)

    companion object : GuildChannelBuilder<GuildTextChannel, TextChannelModifier> {

        override fun create(builder: TextChannelModifier.() -> Unit) = TextChannelModifier(5).apply(builder).build()

    }

    fun asTextChannel(): TextChannel = TextChannelData(guild, data)

    override suspend fun retrieve() = guild.channels.retrieve(id) as NewsChannel

}