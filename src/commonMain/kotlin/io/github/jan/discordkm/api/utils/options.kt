package io.github.jan.discordkm.api.utils

import io.github.jan.discordkm.api.entities.interactions.InteractionOption
import io.github.jan.discordkm.api.entities.interactions.commands.CommandBuilder
import io.github.jan.discordkm.api.entities.interactions.commands.CommandOption
import io.github.jan.discordkm.api.entities.interactions.commands.builders.SlashCommandOptionBuilder
import io.github.jan.discordkm.internal.utils.EnumWithValue

//enum option

/**
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