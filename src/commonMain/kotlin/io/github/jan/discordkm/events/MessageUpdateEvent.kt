package io.github.jan.discordkm.events

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.clients.Intent
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.channels.MessageChannel
import io.github.jan.discordkm.entities.messages.Message

/**
 * Sent when someone updates his message
 *
 * Requires the intent [Intent.GUILD_MESSAGES] or [Intent.DIRECT_MESSAGES]
 */
class MessageUpdateEvent(
    override val client: Client,
    val message: Message,
    override val messageId: Snowflake,
    override val channelId: Snowflake,
    override val channel: MessageChannel
) : MessageEvent