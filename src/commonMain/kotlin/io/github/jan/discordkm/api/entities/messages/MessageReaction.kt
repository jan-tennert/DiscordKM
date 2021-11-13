package io.github.jan.discordkm.api.entities.messages

import io.github.jan.discordkm.api.entities.guild.Emoji
import kotlinx.serialization.Serializable

/**
 * Represents a reaction of a message
 *
 * @param count The amount of users that have reacted to the message
 * @param me Whether the bot has reacted to the message
 * @param emoji The reaction emoji
 */
@Serializable
class MessageReaction(
    val count: Int,
    val me: Boolean,
    val emoji: Emoji
)