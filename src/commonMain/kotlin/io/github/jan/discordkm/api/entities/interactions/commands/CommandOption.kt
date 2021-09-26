/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.interactions.commands

import io.github.jan.discordkm.internal.entities.channels.ChannelType
import io.github.jan.discordkm.internal.utils.valueOfIndex
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class CommandOption(
    @Serializable(with = OptionTypeSerializer::class) val type: OptionType,
    val name: String,
    val description: String,
    @SerialName("required")
    val isRequired: Boolean? = null,
    val choices: List<OptionChoice>? = null,
    val options: List<CommandOption>? = null,
    @SerialName("channel_types")
    val channelTypes: List<ChannelType>? = null,
    val autocomplete: Boolean? = null
) {

    enum class OptionType {
        SUB_COMMAND,
        SUB_COMMAND_GROUP,
        STRING,
        INTEGER,
        BOOLEAN,
        USER,
        CHANNEL,
        ROLE,
        MENTIONABLE,
        NUMBER
    }

}

object ChannelTypeSerializer : KSerializer<ChannelType> {

    override fun deserialize(decoder: Decoder) = valueOfIndex<ChannelType>(decoder.decodeInt())

    override val descriptor = PrimitiveSerialDescriptor("ChannelType", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: ChannelType) {
        encoder.encodeInt(value.id)
    }


}

object OptionTypeSerializer: KSerializer<CommandOption.OptionType> {

    override fun deserialize(decoder: Decoder) = valueOfIndex<CommandOption.OptionType>(decoder.decodeInt())

    override val descriptor = PrimitiveSerialDescriptor("OptionType", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: CommandOption.OptionType) {
        encoder.encodeInt(value.ordinal + 1)
    }


}