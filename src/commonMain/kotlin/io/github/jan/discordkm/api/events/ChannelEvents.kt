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

class ChannelCreateEvent(override val channel: GuildChannel) : ChannelEvent

class ChannelUpdateEvent(override val channel: GuildChannel) : ChannelEvent

class ChannelDeleteEvent(override val channel: GuildChannel) : ChannelEvent

class ChannelPinUpdateEvent(override val channel: GuildTextChannel, val lastPinTimestamp: DateTimeTz?) : ChannelEvent