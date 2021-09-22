package io.github.jan.discordkm.events

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.User
import io.github.jan.discordkm.entities.interactions.Interaction
import io.github.jan.discordkm.entities.lists.OptionList
import io.github.jan.discordkm.entities.messages.Message

class SlashCommandEvent(override val client: Client, override val interaction: Interaction, val command: String, val options: OptionList, val subCommand: String? = null, val subCommandGroup: String? = null) : InteractionCreateEvent

class MessageCommandEvent(override val client: Client, override val interaction: Interaction, val command: String, val targetMessage: Message) : InteractionCreateEvent

class UserCommandEvent(override val client: Client, override val interaction: Interaction, val command: String, val targetUser: User) : InteractionCreateEvent