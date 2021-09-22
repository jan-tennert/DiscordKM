package io.github.jan.discordkm.events

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.clients.Intent
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.User
import io.github.jan.discordkm.entities.channels.MessageChannel
import io.github.jan.discordkm.entities.guild.Emoji
import io.github.jan.discordkm.entities.guild.Member
import io.github.jan.discordkm.restaction.CallsTheAPI

/**
 * Sent when reacts to a message
 *
 * Requires the intent [Intent.GUILD_MESSAGE_REACTIONS] or [Intent.DIRECT_MESSAGE_REACTIONS]
 */
class MessageReactionAddEvent(
    override val client: Client,
    override val channel: MessageChannel,
    override val messageId: Snowflake,
    val emoji: Emoji,
    override val channelId: Snowflake,
    val userId: Snowflake,
    val user: User,
    val member: Member?,
    val guildId: Snowflake?
) : MessageEvent {

    @CallsTheAPI
    suspend fun retrieveGuild() = client.guilds.retrieve(guildId!!)

}