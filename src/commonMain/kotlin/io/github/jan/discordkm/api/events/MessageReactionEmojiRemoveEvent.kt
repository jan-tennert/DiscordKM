package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.Intent
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.internal.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.guild.Emoji


/**
 * Sent when every reaction with the same emojis are deleted
 *
 * Requires the intent [Intent.GUILD_MESSAGE_REACTIONS] or [Intent.DIRECT_MESSAGE_REACTIONS]
 */
class MessageReactionEmojiRemoveEvent(
    override val client: Client,
    override val messageId: Snowflake,
    override val channelId: Snowflake,
    override val channel: MessageChannel,
    val emoji: Emoji,
    val guildId: Snowflake?,
) : MessageEvent {


    suspend fun retrieveGuild() = client.guilds.retrieve(guildId!!)

}