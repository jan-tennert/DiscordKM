/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.events

import com.soywiz.klock.DateTimeTz
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannel
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.GuildTextChannel
import io.github.jan.discordkm.api.entities.clients.Client

interface ChannelEvent : Event {

    val channel: GuildChannel

    override val client: Client
        get() = channel.client

}

/**
 * Sent when a (guild) channel was created
 *
 */
class ChannelCreateEvent(override val channel: GuildChannelCacheEntry) : ChannelEvent

/**
 * Sent when a (guild) channel was updated
 */
class ChannelUpdateEvent(override val channel: GuildChannelCacheEntry, val oldChannel: GuildChannelCacheEntry?) : ChannelEvent

/**
 * Sent when a (guild) channel was deleted
 */
class ChannelDeleteEvent(override val channel: GuildChannelCacheEntry) : ChannelEvent

/**
 * Sent when the pins of a message channel get updated
 */
class ChannelPinUpdateEvent(override val channel: GuildTextChannel, val lastPinTimestamp: DateTimeTz?) : ChannelEvent