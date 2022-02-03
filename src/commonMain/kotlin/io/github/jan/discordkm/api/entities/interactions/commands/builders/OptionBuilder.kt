package io.github.jan.discordkm.api.entities.interactions.commands.builders

import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.interactions.commands.CommandBuilder
import io.github.jan.discordkm.api.entities.interactions.commands.CommandOption
import io.github.jan.discordkm.api.entities.interactions.commands.OptionChoice
import io.github.jan.discordkm.internal.DiscordKMUnstable
import io.github.jan.discordkm.internal.utils.EnumWithValue
import kotlinx.serialization.json.JsonPrimitive

open class OptionBuilder(open val options: MutableList<CommandOption> = mutableListOf()) {

    @CommandBuilder
    fun string(name: String, description: String, required: Boolean = false, autocomplete: Boolean? = null, choices: ChoicesBuilder<String>.() -> Unit = {}) {
        val choiceBuilder = ChoicesBuilder<String>()
        choiceBuilder.choices()
        options += CommandOption(CommandOption.OptionType.STRING, name, description, required, choices = choiceBuilder.choices, autocomplete = autocomplete)
    }

    @CommandBuilder
    fun int(name: String, description: String, required: Boolean = false, max: Int? = null, min: Int? = null, autocomplete: Boolean? = null,  choices: ChoicesBuilder<Int>.() -> Unit = {}) {
        val choiceBuilder = ChoicesBuilder<Int>()
        choiceBuilder.choices()
        options += CommandOption(CommandOption.OptionType.INTEGER, name, description, required, min?.let { JsonPrimitive(it) }, max?.let { JsonPrimitive(it) }, choiceBuilder.choices, autocomplete = autocomplete)
    }

    @CommandBuilder
    fun number(name: String, description: String, required: Boolean = false, max: Double? = null, min: Double? = null, autocomplete: Boolean? = null,  choices: ChoicesBuilder<Double>.() -> Unit = {}) {
        val choiceBuilder = ChoicesBuilder<Double>()
        choiceBuilder.choices()
        options += CommandOption(CommandOption.OptionType.NUMBER, name, description, required, min?.let { JsonPrimitive(it) }, max?.let { JsonPrimitive(it) }, choiceBuilder.choices, autocomplete = autocomplete)
    }

    @CommandBuilder
    @DiscordKMUnstable
    fun attachment(name: String, description: String, required: Boolean = false) { options += CommandOption(CommandOption.OptionType.ATTACHMENT, name, description, required) }

    @CommandBuilder
    fun boolean(name: String, description: String, required: Boolean = false) { options += CommandOption(CommandOption.OptionType.BOOLEAN, name, description, required) }

    @CommandBuilder
    fun user(name: String, description: String, required: Boolean = false) { options += CommandOption(CommandOption.OptionType.USER, name, description, required) }

    @CommandBuilder
    fun mentionable(name: String, description: String, required: Boolean = false) { options += CommandOption(
        CommandOption.OptionType.MENTIONABLE, name, description, required) }

    @CommandBuilder
    fun channel(name: String, description: String, required: Boolean = false, types: List<ChannelType> = emptyList()) { options += CommandOption(
        CommandOption.OptionType.CHANNEL, name, description, required, channelTypes = types) }

    @CommandBuilder
    fun role(name: String, description: String, required: Boolean = false) { options += CommandOption(CommandOption.OptionType.ROLE, name, description, required) }

    /**
     * Generates a [CommandOption] with choices. The enum name will be used as the choice name and the enum value as the choice value. Only works for [Int], [Double] and [String]
     *
     * **not an official type**
     */
    @CommandBuilder
    inline fun <reified T> enum(name: String, description: String, required: Boolean = false) where T : EnumWithValue<*>, T : Enum<T> {
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

    @CommandBuilder
    fun subCommand(name: String, description: String, builder: OptionBuilder.() -> Unit = {}) {
        val options = OptionBuilder()
        options.builder()
        this.options += CommandOption(CommandOption.OptionType.SUB_COMMAND, name, description, false, options = options.options)
    }

    @CommandBuilder
    fun subCommandGroup(name: String, description: String, builder: SubCommandGroupBuilder.() -> Unit) {
        val subCommandGroups = SubCommandGroupBuilder()
        subCommandGroups.builder()
        options += CommandOption(CommandOption.OptionType.SUB_COMMAND_GROUP, name, description, false, options = subCommandGroups.subCommands)
    }

    inner class SubCommandGroupBuilder(internal val subCommands: MutableList<CommandOption> = mutableListOf()) {

        @CommandBuilder
        fun subCommand(
            name: String,
            description: String,
            required: Boolean = false,
            builder: OptionBuilder.() -> Unit = {}
        ) {
            val options = OptionBuilder()
            options.builder()
            subCommands += CommandOption(CommandOption.OptionType.SUB_COMMAND, name, description, required, options = options.options)
        }

    }

    class ChoicesBuilder<T>(val choices: MutableList<OptionChoice> = mutableListOf()) {

        fun choice(name: String, value: T) {
            val primitiveValue = when(value) {
                is Int -> JsonPrimitive(value)
                is Double -> JsonPrimitive(value)
                is String -> JsonPrimitive(value)
                else -> throw IllegalArgumentException("Choices must be of type Int, Double or String")
            }
            choices += OptionChoice(name, primitiveValue)
        }

        fun choice(choice: Pair<String, T>) = choice(choice.first, choice.second)

        infix fun String.value(value: T) = choice(this to value)

    }

}