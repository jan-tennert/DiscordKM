package io.github.jan.discordkm.api.entities.interactions.components

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.events.SelectionMenuEvent
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class SelectionMenu(
    @Required
    override val type: ComponentType = ComponentType.SELECTION_MENU,
    @SerialName("custom_id")
    val customId: String,
    @SerialName("min_values")
    val minValues: Int = 1,
    @SerialName("max_values")
    val maxValues: Int = 1,
    @SerialName("disabled")
    val isDisabled: Boolean = false,
    val options: MutableList<SelectOption>
) : Component

class SelectionMenuBuilder(var customId: String, var minValues: Int, var maxValues: Int, var isDisabled: Boolean, val options: MutableList<SelectOption>) {

    @Transient
    var range: Pair<Int, Int> = minValues to maxValues
        set(value) {
            field = value
            minValues = value.first
            maxValues = value.second
        }

    fun onSelected(client: Client, action: OnSelected) {
        if(client is DiscordWebSocketClient) {
            client.on<SelectionMenuEvent>(predicate = { it.componentId == customId }) { action(this) }
        }
    }

    fun options(builder: OptionBuilder.() -> Unit) {
        options.addAll(OptionBuilder().apply(builder).options)
    }

    fun build() = SelectionMenu(minValues = minValues, maxValues = maxValues, isDisabled = isDisabled, options = options, customId = customId)

    class OptionBuilder(internal val options: MutableList<SelectOption> = mutableListOf()) {

        fun option(label: String = "", value: String = "", description: String? = null, emoji: Emoji? = null, default: Boolean = false) { options += SelectOption(label, value, description, emoji, default)}
        fun add(selectOption: SelectOption) { options += selectOption }

    }

}

typealias OnSelected = suspend SelectionMenuEvent.() -> Unit

fun RowBuilder.selectionMenu(customId: String = "", label: String = "", minValues: Int = 1, maxValues: Int = 1, disabled: Boolean = false, options: List<SelectOption> = emptyList(), builder: SelectionMenuBuilder.() -> Unit) { components += SelectionMenuBuilder(minValues = minValues, maxValues = maxValues, options = options.toMutableList(), isDisabled = disabled, customId = customId).apply(builder).build()}

@Serializable
class SelectOption(val label: String? = null, val value: String, val description: String? = null, val emoji: Emoji? = null, @SerialName("default") val defaultOption: Boolean = false)