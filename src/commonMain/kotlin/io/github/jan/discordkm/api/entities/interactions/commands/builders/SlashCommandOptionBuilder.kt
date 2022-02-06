package io.github.jan.discordkm.api.entities.interactions.commands.builders

import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.interactions.commands.CommandBuilder
import io.github.jan.discordkm.api.entities.interactions.commands.CommandOption
import io.github.jan.discordkm.api.entities.interactions.commands.OptionChoice
import io.github.jan.discordkm.internal.DiscordKMUnstable
import kotlinx.serialization.json.JsonPrimitive

open class SlashCommandOptionBuilder(open val options: MutableList<CommandOption> = mutableListOf()) {

    @CommandBuilder
    fun string(name: String, description: String, required: Boolean = false, autocomplete: Boolean? = null, choices: ChoicesBuilder<String>.() -> Unit = {}) {
        val choiceBuilder = ChoicesBuilder<String>()
        choiceBuilder.choices()
        options += CommandOption(CommandOption.OptionType.STRING, name, description, required, choices = choiceBuilder, autocomplete = autocomplete)
    }

    @CommandBuilder
    fun int(name: String, description: String, required: Boolean = false, max: Int? = null, min: Int? = null, autocomplete: Boolean? = null,  choices: ChoicesBuilder<Int>.() -> Unit = {}) {
        val choiceBuilder = ChoicesBuilder<Int>()
        choiceBuilder.choices()
        options += CommandOption(CommandOption.OptionType.INTEGER, name, description, required, min?.let { JsonPrimitive(it) }, max?.let { JsonPrimitive(it) }, choiceBuilder, autocomplete = autocomplete)
    }

    @CommandBuilder
    fun number(name: String, description: String, required: Boolean = false, max: Double? = null, min: Double? = null, autocomplete: Boolean? = null,  choices: ChoicesBuilder<Double>.() -> Unit = {}) {
        val choiceBuilder = ChoicesBuilder<Double>()
        choiceBuilder.choices()
        options += CommandOption(CommandOption.OptionType.NUMBER, name, description, required, min?.let { JsonPrimitive(it) }, max?.let { JsonPrimitive(it) }, choiceBuilder, autocomplete = autocomplete)
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

    @CommandBuilder
    fun subCommand(name: String, description: String, builder: SlashCommandOptionBuilder.() -> Unit = {}) {
        val options = SlashCommandOptionBuilder()
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
            builder: SlashCommandOptionBuilder.() -> Unit = {}
        ) {
            val options = SlashCommandOptionBuilder()
            options.builder()
            subCommands += CommandOption(CommandOption.OptionType.SUB_COMMAND, name, description, required, options = options.options)
        }

    }

    class ChoicesBuilder<T>(choices: MutableList<OptionChoice> = mutableListOf()) : MutableList<OptionChoice> by choices {

        fun choice(name: String, value: T) {
            val primitiveValue = when(value) {
                is Int -> JsonPrimitive(value)
                is Double -> JsonPrimitive(value)
                is String -> JsonPrimitive(value)
                else -> throw IllegalArgumentException("Choices must be of type Int, Double or String")
            }
            this += OptionChoice(name, primitiveValue)
        }

        fun choice(choice: Pair<String, T>) = choice(choice.first, choice.second)

        infix fun String.value(value: T) = choice(this to value)

    }

}