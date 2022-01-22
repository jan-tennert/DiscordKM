/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.interactions.AutoCompleteInteraction

/**
 * Sent when a user interacts with a slash command where the current option has the **autocomplete** option. This allows bots to send specific choices depending on the input
 */
class AutoCompleteEvent <T>(override val client: Client, override val interaction: AutoCompleteInteraction<T>, val commandName: String, val commandId: Snowflake, val optionName: String, val optionValue: T?, val focused: Boolean, val subCommand: String?, val subCommandGroup: String?) :
    StandardInteractionEvent