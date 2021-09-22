package io.github.jan.discordkm.events

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.clients.Intent
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.channels.MessageChannel
import io.github.jan.discordkm.entities.guild.Emoji
import io.github.jan.discordkm.restaction.CallsTheAPI

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

    @CallsTheAPI
    suspend fun retrieveGuild() = client.guilds.retrieve(guildId!!)

}