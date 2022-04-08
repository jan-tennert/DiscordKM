/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.interactions.components

import io.github.jan.discordkm.api.entities.clients.WSDiscordClient
import io.github.jan.discordkm.api.entities.clients.on
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.events.ButtonClickEvent
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Button(
    @Required
    override val type: ComponentType = ComponentType.BUTTON,
    @SerialName("custom_id")
    override val customId: String = "",
    @SerialName("disabled")
    val isDisabled: Boolean = false,
    val style: ButtonStyle,
    val label: String? = null,
    val emoji: Emoji? = null,
    val url: String? = null
)  : MessageComponent, ComponentWithId

@Serializable(with = ButtonStyle.Companion::class)
enum class ButtonStyle : EnumWithValue<Int> {
    PRIMARY,
    SECONDARY,
    SUCCESS,
    DANGER,
    LINK;

    override val value: Int
        get() = ordinal + 1

    companion object : EnumWithValueGetter<ButtonStyle, Int>(values())
}

inline fun RowBuilder<MessageLayout>.actionButton(style: ButtonStyle, customId: String = "", label: String? = null, emoji: Emoji? = null, isDisabled: Boolean = false, crossinline onClick: suspend ButtonClickEvent.() -> Unit = {}) {
    components += Button(customId = customId, label = label, emoji = emoji, isDisabled = isDisabled, style = style)
    if(client is WSDiscordClient) {
        client.on(predicate = { it.componentId == customId }, onClick)
    }
}

/*
 * A blue button
 * @param customId The id used for identifying the button in the [ButtonClickEvent]
 * @param isDisabled Whether this button should be disabled
 * @param label The label of this button. Can be empty if an emoji is available
 * @param emoji The emoji of this button. Can be empty if a label is available
 */
inline fun RowBuilder<MessageLayout>.primaryButton(customId: String = "", label: String? = null, emoji: Emoji? = null, isDisabled: Boolean = false, crossinline onClick: suspend ButtonClickEvent.() -> Unit = {}) = actionButton(customId = customId, label = label, emoji = emoji, isDisabled = isDisabled, style = ButtonStyle.PRIMARY, onClick = onClick)

/*
 * A red button
 * @param customId The id used for identifying the button in the [ButtonClickEvent]
 * @param isDisabled Whether this button should be disabled
 * @param label The label of this button. Can be empty if an emoji is available
 * @param emoji The emoji of this button. Can be empty if a label is available
 */
inline fun RowBuilder<MessageLayout>.dangerButton(customId: String = "", label: String? = null, emoji: Emoji? = null, isDisabled: Boolean = false, crossinline onClick: suspend ButtonClickEvent.() -> Unit = {}) = actionButton(customId = customId, label = label, emoji = emoji, isDisabled = isDisabled, style = ButtonStyle.DANGER, onClick = onClick)

/*
 * A green button
 * @param customId The id used for identifying the button in the [ButtonClickEvent]
 * @param isDisabled Whether this button should be disabled
 * @param label The label of this button. Can be empty if an emoji is available
 * @param emoji The emoji of this button. Can be empty if a label is available
 */
inline fun RowBuilder<MessageLayout>.successButton(customId: String = "", label: String? = null, emoji: Emoji? = null, isDisabled: Boolean = false, crossinline onClick: suspend ButtonClickEvent.() -> Unit = {}) = actionButton(customId = customId, label = label, emoji = emoji, isDisabled = isDisabled, style = ButtonStyle.SUCCESS, onClick = onClick)

/*
 * A grey button
 * @param customId The id used for identifying the button in the [ButtonClickEvent]
 * @param isDisabled Whether this button should be disabled
 * @param label The label of this button. Can be empty if an emoji is available
 * @param emoji The emoji of this button. Can be empty if a label is available
 */
inline fun RowBuilder<MessageLayout>.secondaryButton(customId: String = "", label: String? = null, emoji: Emoji? = null, isDisabled: Boolean = false, crossinline onClick: suspend ButtonClickEvent.() -> Unit = {}) = actionButton(customId = customId, label = label, emoji = emoji, isDisabled = isDisabled, style = ButtonStyle.SECONDARY, onClick = onClick)

/*
 * A button used for opening urls
 * @param url The url of the button. Used for LinkButtons
 * @param isDisabled Whether this button should be disabled
 * @param label The label of this button. Can be empty if an emoji is available
 * @param emoji The emoji of this button. Can be empty if a label is available
 */
fun RowBuilder<MessageLayout>.linkButton(url: String = "", label: String? = null, emoji: Emoji? = null, isDisabled: Boolean = false) { components += Button(url = url, label = label, emoji = emoji, isDisabled = isDisabled, style = ButtonStyle.LINK) }