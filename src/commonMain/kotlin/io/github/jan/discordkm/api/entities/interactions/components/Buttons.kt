package io.github.jan.discordkm.api.entities.interactions.components

import io.github.jan.discordkm.api.entities.clients.Client
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

class ButtonBuilder(var customId: String? = null, var isDisabled: Boolean, val style: ButtonStyle, var label: String? = null, var emoji: Emoji? = null, var url: String? = null) {

    /**
     * This will be called, if this specific button with this [customId] is pressed
     */
    fun onClick(client: Client, action: ButtonClick) {
        if(client is DiscordWebSocketClient) {
            client.on<ButtonClickEvent>(predicate = { it.componentId == customId}) { action(this) }
        }
    }

    fun build() = Button(style = style, customId = customId, isDisabled = isDisabled, emoji = emoji, url = url, label = label)

}

typealias ButtonClick = suspend ButtonClickEvent.() -> Unit

@Serializable(with = ButtonStyleSerializer::class)
enum class ButtonStyle {
    PRIMARY,
    SECONDARY,
    SUCCESS,
    DANGER,
    LINK
}

/**
 * A blue button
 */
fun RowBuilder.primaryButton(customId: String = "", label: String? = null, emoji: Emoji? = null, isDisabled: Boolean = false, builder: ButtonBuilder.() -> Unit = {}) { components += ButtonBuilder(customId = customId, label = label, emoji = emoji, isDisabled = isDisabled, style = ButtonStyle.PRIMARY).apply(builder).build() }

/**
 * A red button
 */
fun RowBuilder.dangerButton(customId: String = "", label: String? = null, emoji: Emoji? = null, isDisabled: Boolean = false, builder: ButtonBuilder.() -> Unit = {}) { components += ButtonBuilder(customId = customId, label = label, emoji = emoji, isDisabled = isDisabled, style = ButtonStyle.DANGER).apply(builder).build() }

/**
 * A green button
 */
fun RowBuilder.successButton(customId: String = "", label: String? = null, emoji: Emoji? = null, isDisabled: Boolean = false, builder: ButtonBuilder.() -> Unit = {}) { components += ButtonBuilder(customId = customId, label = label, emoji = emoji, isDisabled = isDisabled, style = ButtonStyle.SUCCESS).apply(builder).build() }

/**
 * A grey button
 */
fun RowBuilder.secondaryButton(customId: String = "", label: String? = null, emoji: Emoji? = null, isDisabled: Boolean = false, builder: ButtonBuilder.() -> Unit = {}) { components += ButtonBuilder(customId = customId, label = label, emoji = emoji, isDisabled = isDisabled, style = ButtonStyle.SECONDARY).apply(builder).build() }

/**
 * A button used for opening urls
 */
fun RowBuilder.linkButton(url: String = "", label: String? = null, emoji: Emoji? = null, isDisabled: Boolean = false, builder: ButtonBuilder.() -> Unit = {}) { components += ButtonBuilder(url = url, label = label, emoji = emoji, isDisabled = isDisabled, style = ButtonStyle.LINK).apply(builder).build() }

object ButtonStyleSerializer : KSerializer<ButtonStyle> {
    override fun deserialize(decoder: Decoder) = valueOfIndex<ButtonStyle>(decoder.decodeInt(), 1)

    override val descriptor = PrimitiveSerialDescriptor("ButtonStyle", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: ButtonStyle) {
        encoder.encodeInt(value.ordinal + 1)
    }

}
