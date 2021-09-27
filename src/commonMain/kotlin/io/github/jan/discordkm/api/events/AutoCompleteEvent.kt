package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.interactions.AutoCompleteInteraction

/**
 * Sent when a user interacts with a slash command where the current option has the **autocomplete** option. This allows bots to send specific choices depending on the input
 */
class AutoCompleteEvent(override val client: Client, override val interaction: AutoCompleteInteraction, val commandName: String, val commandId: Snowflake, val optionName: String, val optionValue: String, val focused: Boolean) :
    InteractionCreateEvent