package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.channels.GuildMessageChannel

/**
 * Sent when a webhook gets created, updated or deleted
 */
class WebhooksUpdateEvent(val guild: Guild, val channel: GuildMessageChannel) : Event  {
    override val client: Client
        get() = guild.client
}