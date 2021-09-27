package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.interactions.StandardInteraction
import io.github.jan.discordkm.api.entities.lists.OptionList
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.internal.entities.UserData

interface CommandEvent : InteractionCreateEvent {
    val commandName: String
    override val interaction: StandardInteraction
}

/**
 * Sent when someone enters the slash command
 */
class SlashCommandEvent(override val client: Client, override val interaction: StandardInteraction, override val commandName: String, val options: OptionList, val subCommand: String? = null, val subCommandGroup: String? = null) :
    CommandEvent

/**
 * Sent when someone clicks on a message context menu command
 */
class MessageCommandEvent(override val client: Client, override val interaction: StandardInteraction, override val commandName: String, val targetMessage: Message) :
    CommandEvent

/**
 * Sent when someone clicks on a user context menu command
 */
class UserCommandEvent(override val client: Client, override val interaction: StandardInteraction, override val commandName: String, val targetUser: UserData) :
    CommandEvent
