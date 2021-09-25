package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.Intent
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.internal.entities.channels.MessageChannel

/**
 * Sent when someone deletes multiple messages at once
 *
 * Requires the intent [Intent.GUILD_MESSAGES] or [Intent.DIRECT_MESSAGES]
 */
class MessageBulkDeleteEvent(override val client: Client, val ids: List<Snowflake>, val channel: MessageChannel) : Event