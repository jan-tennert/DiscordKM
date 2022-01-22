/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.interactions.ComponentInteraction
import io.github.jan.discordkm.api.entities.interactions.components.ComponentType
import io.github.jan.discordkm.api.entities.interactions.components.SelectOption

interface ComponentEvent : StandardInteractionEvent {
    override val interaction: ComponentInteraction
    val componentId: String
    val componentType: ComponentType
}

/**
 * Sent when someone clicks on a button, which the bot created
 */
class ButtonClickEvent(
    override val client: Client,
    override val interaction: ComponentInteraction,
    override val componentId: String,
) : ComponentEvent {

    override val componentType: ComponentType = ComponentType.BUTTON

}

/**
 * Sent when someone interacts with a selection menu, which the bot created
 */
class SelectionMenuEvent(
    override val client: Client,
    override val interaction: ComponentInteraction,
    val selectedOptions: List<SelectOption>,
    override val componentId: String,
) : ComponentEvent {

    override val componentType: ComponentType = ComponentType.SELECTION_MENU

}