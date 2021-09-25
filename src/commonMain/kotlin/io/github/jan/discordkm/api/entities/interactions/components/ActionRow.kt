package io.github.jan.discordkm.api.entities.interactions.components

import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

@Serializable
class ActionRow(val components: List<Component> = listOf(), @Required override val type: ComponentType = ComponentType.ACTION_ROW) : Component