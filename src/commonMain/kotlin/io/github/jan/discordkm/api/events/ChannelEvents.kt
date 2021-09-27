package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.channels.GuildChannel

interface ChannelEvent : Event {

    val channel: GuildChannel

    override val client: Client
        get() = channel.client

}

class ChannelCreateEvent(override val channel: GuildChannel) : ChannelEvent

class ChannelUpdateEvent(override val channel: GuildChannel) : ChannelEvent

class ChannelDeleteEvent(override val channel: GuildChannel) : ChannelEvent