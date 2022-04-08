package io.github.jan.discordkm.api.entities.interactions.modals

import io.github.jan.discordkm.api.entities.interactions.ComponentDsl
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
data class TextInputBuilder(
    val style: TextInputStyle,
    @SerialName("custom_id")
    val customId: String = "",
    val label: String = "",
    val placeholder: String? = null,
    val value: String? = null,
    @SerialName("min_length")
    val minLength: Int? = null,
    @SerialName("max_length")
    val maxLength: Int? = null,
    val required: Boolean = true,
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

/*
 * @param customId The id used for identifying the text input value in the [ModalSubmitEvent]
 * @param label The label of the text input
 * @param placeholder The placeholder of the text input
 * @param value The pre-filled value of the text input
 * @param minLength The minimum length of the text input
 * @param maxLength The maximum length of the text input
 * @param required Whether the text input is required in the form
 * @param style The style of the text input
 */
@ComponentDsl
fun RowBuilder<ModalLayout>.textInput(
    customId: String = "",
    label: String = "",
    placeholder: String? = null,
    value: String? = null,
    minLength: Int? = null,
    maxLength: Int? = null,
    required: Boolean = true,
    style: TextInputBuilder.TextInputStyle,
) {
    components += TextInputBuilder(
        style = style,
        customId = customId,
        label = label,
        placeholder = placeholder,
        value = value,
        minLength = minLength,
        maxLength = maxLength,
        required = required,
    )
}

/*
 * A single line text input component
 *
 * @param customId The id used for identifying the text input value in the [ModalSubmitEvent]
 * @param label The label of the text input
 * @param placeholder The placeholder of the text input
 * @param value The pre-filled value of the text input
 * @param minLength The minimum length of the text input
 * @param maxLength The maximum length of the text input
 * @param required Whether the text input is required in the form
 */
@ComponentDsl
fun RowBuilder<ModalLayout>.shortTextInput(
    customId: String = "",
    label: String = "",
    placeholder: String? = null,
    value: String? = null,
    minLength: Int? = null,
    maxLength: Int? = null,
    required: Boolean = true,
) {
    components += TextInputBuilder(
        style = TextInputBuilder.TextInputStyle.SHORT,
        customId = customId,
        label = label,
        placeholder = placeholder,
        value = value,
        minLength = minLength,
        maxLength = maxLength,
        required = required,
    )
}

/*
 * A multi-line text input component
 *
 * @param customId The id used for identifying the text input value in the [ModalSubmitEvent]
 * @param label The label of the text input
 * @param placeholder The placeholder of the text input
 * @param value The pre-filled value of the text input
 * @param minLength The minimum length of the text input
 * @param maxLength The maximum length of the text input
 * @param required Whether the text input is required in the form
 */
@ComponentDsl
fun RowBuilder<ModalLayout>.multilineTextInput(
    customId: String = "",
    label: String = "",
    placeholder: String? = null,
    value: String? = null,
    minLength: Int? = null,
    maxLength: Int? = null,
    required: Boolean = true,
) {
    components += TextInputBuilder(
        style = TextInputBuilder.TextInputStyle.PARAGRAPH,
        customId = customId,
        label = label,
        placeholder = placeholder,
        value = value,
        minLength = minLength,
        maxLength = maxLength,
        required = required,
    )
}


