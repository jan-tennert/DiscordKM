package io.github.jan.discordkm.events.internal

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.User
import io.github.jan.discordkm.entities.guild.Role
import io.github.jan.discordkm.entities.interactions.Interaction
import io.github.jan.discordkm.entities.interactions.commands.ApplicationCommandType
import io.github.jan.discordkm.entities.interactions.commands.CommandOption
import io.github.jan.discordkm.entities.lists.OptionList
import io.github.jan.discordkm.events.InteractionCreateEvent
import io.github.jan.discordkm.events.SlashCommandEvent
import io.github.jan.discordkm.utils.extractChannel
import io.github.jan.discordkm.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class InteractionCreateEventHandler(val client: Client) : InternalEventHandler<InteractionCreateEvent> {

    override fun handle(data: JsonObject): InteractionCreateEvent {
        return when {
            data.getValue("data").jsonObject.contains("type") -> {
                extractApplicationCommand(data)
            }
            else -> throw IllegalStateException()
        }
    }

    private fun extractApplicationCommand(data: JsonObject) : InteractionCreateEvent {
        val type = ApplicationCommandType.values().first { it.ordinal + 1 == data.getValue("data").jsonObject.getOrThrow<Int>("type") }
        return when(type) {
            ApplicationCommandType.CHAT_INPUT -> extractChatInputCommand(data)
            ApplicationCommandType.USER -> TODO()
            ApplicationCommandType.MESSAGE -> TODO()
        }
    }

    private fun extractChatInputCommand(data: JsonObject) : InteractionCreateEvent {
        var subCommandGroup: String? = null
        var subCommand: String? = null
        val resolved = data.getValue("data").jsonObject["resolved"]?.jsonObject
        val options = mutableListOf<Interaction.InteractionOption>()
        data.getValue("data").jsonObject.getValue("options").jsonArray.forEach { option ->
            option as JsonObject
            when(CommandOption.OptionType.values().first { it.ordinal + 1 == option.getOrThrow<Int>("type") }) {
                CommandOption.OptionType.SUB_COMMAND -> {
                    subCommand = option.jsonObject.getOrThrow<String>("name")
                    option["options"]?.jsonArray?.forEach {
                        options += extractOption(resolved!!, it.jsonObject, data)
                    }
                }
                CommandOption.OptionType.SUB_COMMAND_GROUP -> TODO()
                else -> options += extractOption(resolved!!, option.jsonObject, data)
            }
        }
        return SlashCommandEvent(client, Interaction(client, data), OptionList(options))
    }

    private fun extractOption(resolved: JsonObject, option: JsonObject, fullData: JsonObject) : Interaction.InteractionOption {
        val type = CommandOption.OptionType.values().first { it.ordinal + 1 == option.jsonObject.getOrThrow<Int>("type") }
        val name = option.getOrThrow<String>("name")
        val guild = client.guilds[fullData.getOrThrow<Snowflake>(
            "guild_id"
        )]
        return when(type) {
            CommandOption.OptionType.STRING -> Interaction.InteractionOption(name, option.getOrThrow<String>("value"))
            CommandOption.OptionType.INTEGER -> Interaction.InteractionOption(name, option.getOrThrow<Int>("value"))
            CommandOption.OptionType.BOOLEAN -> Interaction.InteractionOption(name, option.getOrThrow<Boolean>("value"))
            CommandOption.OptionType.USER -> Interaction.InteractionOption(name, User(client, resolved.getValue("users").jsonObject.getValue(option.getOrThrow<Snowflake>("value").string).jsonObject))
            CommandOption.OptionType.CHANNEL -> Interaction.InteractionOption(name, resolved.getValue("channels").jsonObject.getValue(option.getOrThrow<Snowflake>("value").string).jsonObject.extractChannel(client, guild))
            CommandOption.OptionType.ROLE -> Interaction.InteractionOption(name, Role(guild!!, resolved.getValue("roles").jsonObject.getValue(option.getOrThrow<Snowflake>("value").string).jsonObject))
            CommandOption.OptionType.MENTIONABLE -> TODO()
            CommandOption.OptionType.NUMBER -> Interaction.InteractionOption(name, option.getOrThrow<Double>("value"))
            else -> throw IllegalStateException()
        }
    }


}