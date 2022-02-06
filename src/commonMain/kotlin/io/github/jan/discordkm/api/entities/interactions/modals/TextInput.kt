package io.github.jan.discordkm.api.entities.interactions.modals

import io.github.jan.discordkm.api.entities.interactions.components.Component
import io.github.jan.discordkm.api.entities.interactions.components.ComponentType
import io.github.jan.discordkm.api.entities.interactions.components.RowBuilder
import io.github.jan.discordkm.api.events.ModalSubmitEvent
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TextInput(
    var style: TextInputStyle,
    @SerialName("custom_id")
    var customId: String = "",
    var label: String = "",
    var placeholder: String? = null,
    var value: String? = null,
    @SerialName("min_length")
    var minLength: Int? = null,
    @SerialName("max_length")
    var maxLength: Int? = null,
) : Component {

    @Required
    override val type: ComponentType = ComponentType.TEXT_INPUT

    @Serializable(with = TextInputStyle.Companion::class)
    enum class TextInputStyle : EnumWithValue<Int> {
        SHORT,
        PARAGRAPH;

        override val value: Int
            get() = ordinal + 1

        companion object : EnumWithValueGetter<TextInputStyle, Int>(values())
    }

}

/**
 * @param customId The id used for identifying the text input value in the [ModalSubmitEvent]
 * @param label The label of the text input
 * @param placeholder The placeholder of the text input
 * @param value The pre-filled value of the text input
 * @param minLength The minimum length of the text input
 * @param maxLength The maximum length of the text input
 * @param style The style of the text input
 */
inline fun RowBuilder<ModalLayout>.textInput(
    customId: String = "",
    label: String = "",
    placeholder: String? = null,
    value: String? = null,
    minLength: Int? = null,
    maxLength: Int? = null,
    style: TextInput.TextInputStyle = TextInput.TextInputStyle.SHORT,
) {
    components += TextInput(
        style = style,
        customId = customId,
        label = label,
        placeholder = placeholder,
        value = value,
        minLength = minLength,
        maxLength = maxLength,
    )
}

/**
 * A single line text input component
 *
 * @param customId The id used for identifying the text input value in the [ModalSubmitEvent]
 * @param label The label of the text input
 * @param placeholder The placeholder of the text input
 * @param value The pre-filled value of the text input
 * @param minLength The minimum length of the text input
 * @param maxLength The maximum length of the text input
 */
inline fun RowBuilder<ModalLayout>.shortTextInput(
    customId: String = "",
    label: String = "",
    placeholder: String? = null,
    value: String? = null,
    minLength: Int? = null,
    maxLength: Int? = null,
) {
    components += TextInput(
        style = TextInput.TextInputStyle.SHORT,
        customId = customId,
        label = label,
        placeholder = placeholder,
        value = value,
        minLength = minLength,
        maxLength = maxLength
    )
}

/**
 * A multi-line text input component
 *
 * @param customId The id used for identifying the text input value in the [ModalSubmitEvent]
 * @param label The label of the text input
 * @param placeholder The placeholder of the text input
 * @param value The pre-filled value of the text input
 * @param minLength The minimum length of the text input
 * @param maxLength The maximum length of the text input
 */
inline fun RowBuilder<ModalLayout>.multilineTextInput(
    customId: String = "",
    label: String = "",
    placeholder: String? = null,
    value: String? = null,
    minLength: Int? = null,
    maxLength: Int? = null,
) {
    components += TextInput(
        style = TextInput.TextInputStyle.PARAGRAPH,
        customId = customId,
        label = label,
        placeholder = placeholder,
        value = value,
        minLength = minLength,
        maxLength = maxLength
    )
}


