/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.interactions.commands

import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class CommandOption(
    @Serializable(with = OptionType.Companion::class) val type: OptionType,
    val name: String,
    val description: String,
    @SerialName("required")
    val isRequired: Boolean? = null,
    @SerialName("min_value")
    val minValue: JsonPrimitive? = null,
    @SerialName("max_value")
    val maxValue: JsonPrimitive? = null,
    val choices: List<OptionChoice>? = null,
    val options: List<CommandOption>? = null,
    @SerialName("channel_types")
    val channelTypes: List<ChannelType>? = null,
    val autocomplete: Boolean? = null,
    @SerialName("name_localizations")
    val nameLocalizations: JsonObject? = null,
    @SerialName("description_localizations")
    val descriptionLocalizations: JsonObject? = null
) {

    enum class OptionType : EnumWithValue<Int> {
        SUB_COMMAND,
        SUB_COMMAND_GROUP,
        STRING,
        INTEGER,
        BOOLEAN,
        USER,
        CHANNEL,
        ROLE,
        MENTIONABLE,
        NUMBER,
        ATTACHMENT;

        override val value: Int
            get() = ordinal + 1

        companion object : EnumWithValueGetter<OptionType, Int>(values())
    }

}