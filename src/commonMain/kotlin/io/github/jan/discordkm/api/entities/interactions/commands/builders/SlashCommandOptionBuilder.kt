/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.interactions.commands.builders

import io.github.jan.discordkm.api.entities.DiscordLocale
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.interactions.commands.CommandBuilder
import io.github.jan.discordkm.api.entities.interactions.commands.CommandOption
import io.github.jan.discordkm.api.entities.interactions.commands.OptionChoice
import io.github.jan.discordkm.api.entities.misc.TranslationManager
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

open class SlashCommandOptionBuilder(
    private val translationManager: TranslationManager?,
    private val commandName: String,
    private val subCommand: String? = null,
    private val subCommandGroup: String? = null,
    open val options: MutableList<CommandOption> = mutableListOf()
) {

    @CommandBuilder
    fun string(
        name: String,
        description: String,
        required: Boolean = false,
        autocomplete: Boolean? = null,
        translations: TranslationBuilder.() -> Unit = { },
        choices: ChoicesBuilder<String>.() -> Unit = {}
    ) {
        val choiceBuilder = ChoicesBuilder<String>()
        val translation = TranslationBuilder().apply(translations).applyFileTranslations(name)
        choiceBuilder.choices()
        options += CommandOption(
            CommandOption.OptionType.STRING,
            name,
            description,
            required,
            choices = choiceBuilder,
            autocomplete = autocomplete,
            nameLocalizations = translation.name.format(),
            descriptionLocalizations = translation.description.format()
        )
    }

    @CommandBuilder
    fun int(
        name: String,
        description: String,
        required: Boolean = false,
        max: Int? = null,
        min: Int? = null,
        autocomplete: Boolean? = null,
        translations: TranslationBuilder.() -> Unit = { },
        choices: ChoicesBuilder<Int>.() -> Unit = {}
    ) {
        val choiceBuilder = ChoicesBuilder<Int>()
        val translation = TranslationBuilder().apply(translations).applyFileTranslations(name)
        choiceBuilder.choices()
        options += CommandOption(
            CommandOption.OptionType.INTEGER,
            name,
            description,
            required,
            min?.let { JsonPrimitive(it) },
            max?.let { JsonPrimitive(it) },
            choiceBuilder,
            autocomplete = autocomplete,
            nameLocalizations = translation.name.format(),
            descriptionLocalizations = translation.description.format()
        )
    }

    @CommandBuilder
    fun number(
        name: String,
        description: String,
        required: Boolean = false,
        max: Double? = null,
        min: Double? = null,
        translations: TranslationBuilder.() -> Unit = { },
        autocomplete: Boolean? = null,
        choices: ChoicesBuilder<Double>.() -> Unit = {}
    ) {
        val choiceBuilder = ChoicesBuilder<Double>()
        val translation = TranslationBuilder().apply(translations).applyFileTranslations(name)
        choiceBuilder.choices()
        options += CommandOption(
            CommandOption.OptionType.NUMBER,
            name,
            description,
            required,
            min?.let { JsonPrimitive(it) },
            max?.let { JsonPrimitive(it) },
            choiceBuilder,
            autocomplete = autocomplete,
            nameLocalizations = translation.name.format(),
            descriptionLocalizations = translation.description.format()
        )
    }

    @CommandBuilder
    fun attachment(
        name: String,
        description: String,
        required: Boolean = false,
        translations: TranslationBuilder.() -> Unit = { }
    ) {
        val translation = TranslationBuilder().apply(translations).applyFileTranslations(name)
        options += CommandOption(
            CommandOption.OptionType.ATTACHMENT,
            name,
            description,
            required,
            nameLocalizations = translation.name.format(),
            descriptionLocalizations = translation.description.format()
        )
    }

    @CommandBuilder
    fun boolean(
        name: String,
        description: String,
        required: Boolean = false,
        translations: TranslationBuilder.() -> Unit = { }
    ) {
        val translation = TranslationBuilder().apply(translations).applyFileTranslations(name)
        options += CommandOption(
            CommandOption.OptionType.BOOLEAN,
            name,
            description,
            required,
            nameLocalizations = translation.name.format(),
            descriptionLocalizations = translation.description.format()
        )
    }

    @CommandBuilder
    fun user(
        name: String,
        description: String,
        required: Boolean = false,
        translations: TranslationBuilder.() -> Unit = { }
    ) {
        val translation = TranslationBuilder().apply(translations).applyFileTranslations(name)
        options += CommandOption(
            CommandOption.OptionType.USER,
            name,
            description,
            required,
            nameLocalizations = translation.name.format(),
            descriptionLocalizations = translation.description.format()
        )
    }

    @CommandBuilder
    fun mentionable(
        name: String,
        description: String,
        required: Boolean = false,
        translations: TranslationBuilder.() -> Unit = { }
    ) {
        val translation = TranslationBuilder().apply(translations).applyFileTranslations(name)
        options += CommandOption(
            CommandOption.OptionType.MENTIONABLE,
            name,
            description,
            required,
            nameLocalizations = translation.name.format(),
            descriptionLocalizations = translation.description.format()
        )
    }

    @CommandBuilder
    fun channel(
        name: String,
        description: String,
        required: Boolean = false,
        types: List<ChannelType> = emptyList(),
        translations: TranslationBuilder.() -> Unit = { }
    ) {
        val translation = TranslationBuilder().apply(translations).applyFileTranslations(name)
        options += CommandOption(
            CommandOption.OptionType.CHANNEL,
            name,
            description,
            required,
            channelTypes = types,
            nameLocalizations = translation.name.format(),
            descriptionLocalizations = translation.description.format()
        )
    }

    @CommandBuilder
    fun role(
        name: String,
        description: String,
        required: Boolean = false,
        translations: TranslationBuilder.() -> Unit = { }
    ) {
        val translation = TranslationBuilder().apply(translations).applyFileTranslations(name)
        options += CommandOption(
            CommandOption.OptionType.ROLE,
            name,
            description,
            required,
            nameLocalizations = translation.name.format(),
            descriptionLocalizations = translation.description.format()
        )
    }

    @CommandBuilder
    fun subCommand(
        name: String,
        description: String,
        translations: TranslationBuilder.() -> Unit = { },
        builder: SlashCommandOptionBuilder.() -> Unit = {}
    ) {
        val options = SlashCommandOptionBuilder(translationManager, commandName, name)
        val translation = TranslationBuilder().apply(translations).applyFileTranslations(name)
        options.builder()
        this.options += CommandOption(
            CommandOption.OptionType.SUB_COMMAND,
            name,
            description,
            false,
            options = options.options,
            nameLocalizations = translation.name.format(),
            descriptionLocalizations = translation.description.format()
        )
    }

    @CommandBuilder
    fun subCommandGroup(
        name: String,
        description: String,
        translations: TranslationBuilder.() -> Unit = { },
        builder: SubCommandGroupBuilder.() -> Unit
    ) {
        val subCommandGroups = SubCommandGroupBuilder(translationManager, commandName, subCommandGroupName = name)
        val translation = TranslationBuilder().apply(translations).applyFileTranslations(name)
        subCommandGroups.builder()
        options += CommandOption(
            CommandOption.OptionType.SUB_COMMAND_GROUP,
            name,
            description,
            false,
            options = subCommandGroups.subCommands,
            nameLocalizations = translation.name.format(),
            descriptionLocalizations = translation.description.format()
        )
    }

    private fun TranslationBuilder.applyFileTranslations(name: String) : TranslationBuilder {
        val key = when {
            subCommandGroup != null -> "application.command.$commandName.options.$subCommandGroup.options.$subCommand.options.$name"
            subCommand != null -> "application.command.$commandName.options.$subCommand.options.$name"
            else -> "application.command.$commandName.options.$name"
        }
        translationManager?.let {
            this.name.putAll(it.getAll("$key.name"))
            this.description.putAll(it.getAll("$key.description"))
        }
        return this
    }

    private fun Map<DiscordLocale, String>.format() = JsonObject(map { it.key.value to JsonPrimitive(it.value) }.toMap())

    inner class SubCommandGroupBuilder(private val translationManager: TranslationManager?, val commandName: String, val subCommandGroupName: String, internal val subCommands: MutableList<CommandOption> = mutableListOf()) {

        @CommandBuilder
        fun subCommand(
            name: String,
            description: String,
            required: Boolean = false,
            builder: SlashCommandOptionBuilder.() -> Unit = {}
        ) {
            val options = SlashCommandOptionBuilder(translationManager, commandName, subCommandGroup = subCommandGroupName, subCommand = name)
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