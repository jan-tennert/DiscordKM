package io.github.jan.discordkm.api.events

import com.soywiz.klock.DateTimeTz
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.channels.GuildChannel
import io.github.jan.discordkm.api.entities.guild.channels.GuildTextChannel

interface ChannelEvent : Event {

    val channel: GuildChannel

    override val client: Client
        get() = channel.client

}

/**
 * Sent when a (guild) channel was created
 *
 */
class ChannelCreateEvent(override val channel: GuildChannel) : ChannelEvent

/**
 * Sent when a (guild) channel was updated
 */
class ChannelUpdateEvent(override val channel: GuildChannel, val oldChannel: GuildChannel?) : ChannelEvent

/**
 * Sent when a (guild) channel was deleted
 */
class ChannelDeleteEvent(override val channel: GuildChannel) : ChannelEvent

/**
 * Sent when the pins of a message channel get updated
 */
class ChannelPinUpdateEvent(override val channel: GuildTextChannel, val lastPinTimestamp: DateTimeTz?) : ChannelEvent