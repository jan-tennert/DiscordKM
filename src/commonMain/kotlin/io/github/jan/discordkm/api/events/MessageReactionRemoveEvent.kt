package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.Intent
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.internal.entities.channels.MessageChannel


/**
 * Sent when someone removes a reaction
 *
 * Requires the intent [Intent.GUILD_MESSAGE_REACTIONS] or [Intent.DIRECT_MESSAGE_REACTIONS]
 */
class MessageReactionRemoveEvent(
    override val client: Client,
    override val channel: MessageChannel,
    override val messageId: Snowflake,
    val emoji: Emoji,
    override val channelId: Snowflake,
    val userId: Snowflake,
    val user: User,
    val guildId: Snowflake?
) : MessageEvent {


    suspend fun retrieveGuild() = client.guilds.retrieve(guildId!!)

}