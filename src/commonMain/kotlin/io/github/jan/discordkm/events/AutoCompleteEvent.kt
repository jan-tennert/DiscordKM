package io.github.jan.discordkm.events

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.interactions.AutoCompleteInteraction

class AutoCompleteEvent(override val client: Client, override val interaction: AutoCompleteInteraction, val commandName: String, val commandId: Snowflake, val optionName: String, val optionValue: String, val focused: Boolean) : InteractionCreateEvent