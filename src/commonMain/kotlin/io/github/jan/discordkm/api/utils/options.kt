/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.utils

import io.github.jan.discordkm.api.entities.interactions.InteractionOption
import io.github.jan.discordkm.api.entities.interactions.commands.CommandBuilder
import io.github.jan.discordkm.api.entities.interactions.commands.CommandOption
import io.github.jan.discordkm.api.entities.interactions.commands.builders.SlashCommandOptionBuilder
import io.github.jan.discordkm.internal.utils.EnumWithValue

//enum option

/*
 * Generates a [CommandOption] with choices. The enum name will be used as the choice name and the enum value as the choice value. Only works for [Int], [Double] and [String]
 *
 * **not an official type**
 */
@CommandBuilder
inline fun <reified T> SlashCommandOptionBuilder.enum(name: String, description: String, required: Boolean = false) where T : EnumWithValue<*>, T : Enum<T> {
    val values = enumValues<T>()
    when(values[0].value) {
        is Int -> int(name, description, required) {
            values.forEach { enum -> choice(enum.name, enum.value as Int) }
        }
        is Double -> number(name, description, required) {
            values.forEach { enum -> choice(enum.name, enum.value as Double) }
        }
        is String -> string(name, description, required) {
            values.forEach { enum -> choice(enum.name, enum.value as String) }
        }
        else -> throw IllegalArgumentException("Enum values must be of type Int, Double or String")
    }
}

inline fun <reified T> InteractionOption.asEnum() where T : EnumWithValue<*>, T : Enum<T> = enumValues<T>().first { it.value == value }

/*-----------------------------------------------------*/