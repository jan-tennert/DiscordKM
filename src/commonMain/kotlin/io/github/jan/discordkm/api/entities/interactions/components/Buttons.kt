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
import io.github.jan.discordkm.api.events.ButtonClickEvent
import io.github.jan.discordkm.internal.utils.valueOfIndex
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Button(
    @Required
    override val type: ComponentType = ComponentType.BUTTON,
    @SerialName("custom_id")
    var customId: String? = null,
    @SerialName("disabled")
    var isDisabled: Boolean = false,
    val style: ButtonStyle,
    var label: String? = null,
    var emoji: Emoji? = null,
    var url: String? = null
)  : Component

@Serializable(with = ButtonStyleSerializer::class)
enum class ButtonStyle {
    PRIMARY,
    SECONDARY,
    SUCCESS,
    DANGER,
    LINK
}

fun RowBuilder<MessageLayout>.actionButton(style: ButtonStyle, customId: String = "", label: String? = null, emoji: Emoji? = null, isDisabled: Boolean = false, onClick: suspend ButtonClickEvent.() -> Unit = {}) {
    components += Button(customId = customId, label = label, emoji = emoji, isDisabled = isDisabled, style = style)
    if(client is DiscordWebSocketClient) {
        client.on<ButtonClickEvent>(predicate = { it.componentId == customId }, onClick)
    }
}

/**
 * A blue button
 * @param customId The id used for identifying the button in the [ButtonClickEvent]
 * @param isDisabled Whether this button should be disabled
 * @param label The label of this button. Can be empty if an emoji is available
 * @param emoji The emoji of this button. Can be empty if a label is available
 */
fun RowBuilder<MessageLayout>.primaryButton(customId: String = "", label: String? = null, emoji: Emoji? = null, isDisabled: Boolean = false, onClick: suspend ButtonClickEvent.() -> Unit = {}) = actionButton(customId = customId, label = label, emoji = emoji, isDisabled = isDisabled, style = ButtonStyle.PRIMARY, onClick = onClick)

/**
 * A red button
 * @param customId The id used for identifying the button in the [ButtonClickEvent]
 * @param isDisabled Whether this button should be disabled
 * @param label The label of this button. Can be empty if an emoji is available
 * @param emoji The emoji of this button. Can be empty if a label is available
 */
fun RowBuilder<MessageLayout>.dangerButton(customId: String = "", label: String? = null, emoji: Emoji? = null, isDisabled: Boolean = false, onClick: suspend ButtonClickEvent.() -> Unit = {}) = actionButton(customId = customId, label = label, emoji = emoji, isDisabled = isDisabled, style = ButtonStyle.DANGER, onClick = onClick)

/**
 * A green button
 * @param customId The id used for identifying the button in the [ButtonClickEvent]
 * @param isDisabled Whether this button should be disabled
 * @param label The label of this button. Can be empty if an emoji is available
 * @param emoji The emoji of this button. Can be empty if a label is available
 */
fun RowBuilder<MessageLayout>.successButton(customId: String = "", label: String? = null, emoji: Emoji? = null, isDisabled: Boolean = false, onClick: suspend ButtonClickEvent.() -> Unit = {}) = actionButton(customId = customId, label = label, emoji = emoji, isDisabled = isDisabled, style = ButtonStyle.SUCCESS, onClick = onClick)

/**
 * A grey button
 * @param customId The id used for identifying the button in the [ButtonClickEvent]
 * @param isDisabled Whether this button should be disabled
 * @param label The label of this button. Can be empty if an emoji is available
 * @param emoji The emoji of this button. Can be empty if a label is available
 */
fun RowBuilder<MessageLayout>.secondaryButton(customId: String = "", label: String? = null, emoji: Emoji? = null, isDisabled: Boolean = false, onClick: suspend ButtonClickEvent.() -> Unit = {}) = actionButton(customId = customId, label = label, emoji = emoji, isDisabled = isDisabled, style = ButtonStyle.SECONDARY, onClick = onClick)

/**
 * A button used for opening urls
 * @param url The url of the button. Used for LinkButtons
 * @param isDisabled Whether this button should be disabled
 * @param label The label of this button. Can be empty if an emoji is available
 * @param emoji The emoji of this button. Can be empty if a label is available
 */
fun RowBuilder<MessageLayout>.linkButton(url: String = "", label: String? = null, emoji: Emoji? = null, isDisabled: Boolean = false) { components += Button(url = url, label = label, emoji = emoji, isDisabled = isDisabled, style = ButtonStyle.LINK) }

object ButtonStyleSerializer : KSerializer<ButtonStyle> {
    override fun deserialize(decoder: Decoder) = valueOfIndex<ButtonStyle>(decoder.decodeInt(), 1)

    override val descriptor = PrimitiveSerialDescriptor("ButtonStyle", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: ButtonStyle) {
        encoder.encodeInt(value.ordinal + 1)
    }

}
