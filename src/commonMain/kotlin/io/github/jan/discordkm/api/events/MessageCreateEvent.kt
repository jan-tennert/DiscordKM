package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.Intent
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.internal.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.messages.Message

/**
 * Sent when someone sends a message into a message channel
 *
 * Requires the intent [Intent.GUILD_MESSAGES] or [Intent.DIRECT_MESSAGES]
 */
class MessageCreateEvent(
    override val client: Client, val message: Message,
    override val messageId: Snowflake,
    override val channelId: Snowflake,
    override val channel: MessageChannel
) : MessageEvent