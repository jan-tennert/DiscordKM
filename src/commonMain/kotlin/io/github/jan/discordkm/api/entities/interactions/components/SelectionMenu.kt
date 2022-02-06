/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.interactions.components

import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.events.SelectionMenuEvent
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SelectionMenu(
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

class SelectionMenuOptionBuilder(val options: MutableList<SelectOption> = mutableListOf()) {

    fun option(label: String = "", value: String = "", description: String? = null, emoji: Emoji? = null, default: Boolean = false) { options += SelectOption(label, value, description, emoji, default)}
    fun add(selectOption: SelectOption) { options += selectOption }
    fun addAll(selectOptions: Iterable<SelectOption>) { options += selectOptions }
    fun addAll(selectOptions: Array<SelectOption>) { options += selectOptions }

}

/**
 * A selection menu
 * @param customId The id of the selection menu. Used for the [SelectionMenuEvent]
 * @param minValues The minimum amount of values the user has to select
 * @param maxValues The maximum amount of values the user can select
 * @param isDisabled Whether the user can interact with this selection menu
 * @param options The options of the selection menu
*/
inline fun RowBuilder<MessageLayout>.selectionMenu(customId: String = "", isDisabled: Boolean = false, range: Pair<Int, Int>, options: SelectionMenuOptionBuilder.() -> Unit, crossinline onSelection: suspend SelectionMenuEvent.() -> Unit) {
    components += SelectionMenu(minValues = range.first, maxValues = range.second, options = SelectionMenuOptionBuilder().apply(options).options, isDisabled = isDisabled, customId = customId)
    if(client is DiscordWebSocketClient) {
        client.on(predicate = { it.componentId == customId }, onSelection)
    }
}

/**
 * @param label The label of the selection menu option
 * @param value The value of the selection menu option (used for identifying the selected option in [SelectionMenuEvent])
 * @param emoji The emoji of the selection menu option
 * @param defaultOption Whether this selection menu option should be the default option
 */
@Serializable
class SelectOption(val label: String? = null, val value: String, val description: String? = null, val emoji: Emoji? = null, @SerialName("default") val defaultOption: Boolean = false)