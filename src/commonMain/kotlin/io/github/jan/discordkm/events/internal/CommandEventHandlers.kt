package io.github.jan.discordkm.events.internal

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.User
import io.github.jan.discordkm.entities.guild.Role
import io.github.jan.discordkm.entities.interactions.Interaction
import io.github.jan.discordkm.entities.interactions.commands.ApplicationCommandType
import io.github.jan.discordkm.entities.interactions.commands.CommandOption
import io.github.jan.discordkm.entities.lists.OptionList
import io.github.jan.discordkm.entities.messages.Message
import io.github.jan.discordkm.events.InteractionCreateEvent
import io.github.jan.discordkm.events.MessageCommandEvent
import io.github.jan.discordkm.events.SlashCommandEvent
import io.github.jan.discordkm.events.UserCommandEvent
import io.github.jan.discordkm.utils.extractChannel
import io.github.jan.discordkm.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class InteractionCreateEventHandler(val client: Client) : InternalEventHandler<InteractionCreateEvent> {

    override fun handle(data: JsonObject): InteractionCreateEvent {
        println(data)
        return when {
            data.getValue("data").jsonObject.contains("type") -> {
                extractApplicationCommand(data)
            }
            else -> throw IllegalStateException()
        }
    }

    private fun extractApplicationCommand(data: JsonObject) = when(ApplicationCommandType.values().first { it.ordinal + 1 == data.getValue("data").jsonObject.getOrThrow<Int>("type") }) {
        ApplicationCommandType.CHAT_INPUT -> extractChatInputCommand(data)
        ApplicationCommandType.USER -> extractUserCommand(data)
        ApplicationCommandType.MESSAGE -> extractMessageCommand(data)
    }

    private fun extractMessageCommand(data: JsonObject) : MessageCommandEvent {
        val target = data.getValue("data").jsonObject.getOrThrow<String>("target_id")
        val interaction = Interaction(client, data)
        val name = data.getValue("data").jsonObject.getOrThrow<String>("name")
        val message = Message(interaction.channel!!, data.getValue("data").jsonObject.getValue("resolved").jsonObject.getValue("messages").jsonObject.getValue(target).jsonObject)
        return MessageCommandEvent(client, interaction, name, message)
    }

    private fun extractUserCommand(data: JsonObject) : UserCommandEvent {
        val target = data.getValue("data").jsonObject.getOrThrow<String>("target_id")
        val interaction = Interaction(client, data)
        val name = data.getValue("data").jsonObject.getOrThrow<String>("name")
        val user = User(client, data.getValue("data").jsonObject.getValue("resolved").jsonObject.jsonObject.getValue("users").jsonObject.getValue(target).jsonObject)
        return UserCommandEvent(client, interaction, name, user)
    }

    private fun extractChatInputCommand(data: JsonObject) : InteractionCreateEvent {
        var subCommandGroup: String? = null
        var subCommand: String? = null
        val resolved = data.getValue("data").jsonObject["resolved"]?.jsonObject
        val name = data.getValue("data").jsonObject.getOrThrow<String>("name")
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
                CommandOption.OptionType.SUB_COMMAND_GROUP -> {
                    subCommandGroup = option.jsonObject.getOrThrow<String>("name")
                    subCommand = option.getValue("options").jsonArray[0].jsonObject.getOrThrow<String>("name")
                    option.getValue("options").jsonArray[0].jsonObject["options"]?.jsonArray?.forEach {
                        options += extractOption(resolved!!, it.jsonObject, data)
                    }
                }
                else -> options += extractOption(resolved!!, option.jsonObject, data)
            }
        }
        return SlashCommandEvent(client, Interaction(client, data), name, OptionList(options), subCommand, subCommandGroup)
    }

    private fun extractOption(resolved: JsonObject, option: JsonObject, fullData: JsonObject) : Interaction.InteractionOption {
        val type = CommandOption.OptionType.values().first { it.ordinal + 1 == option.jsonObject.getOrThrow<Int>("type") }
        val name = option.getOrThrow<String>("name")
        val guild = client.guilds[fullData.getOrThrow<Snowflake>(
            "guild_id"
        )]
        return when(type) {
            CommandOption.OptionType.STRING -> Interaction.InteractionOption(name, type, option.getOrThrow<String>("value"))
            CommandOption.OptionType.INTEGER -> Interaction.InteractionOption(name, type, option.getOrThrow<Int>("value"))
            CommandOption.OptionType.BOOLEAN -> Interaction.InteractionOption(name, type, option.getOrThrow<Boolean>("value"))
            CommandOption.OptionType.USER -> Interaction.InteractionOption(name, type, User(client, resolved.getValue("users").jsonObject.getValue(option.getOrThrow<Snowflake>("value").string).jsonObject))
            CommandOption.OptionType.CHANNEL -> Interaction.InteractionOption(name, type, resolved.getValue("channels").jsonObject.getValue(option.getOrThrow<Snowflake>("value").string).jsonObject.extractChannel(client, guild))
            CommandOption.OptionType.ROLE -> Interaction.InteractionOption(name, type, Role(guild!!, resolved.getValue("roles").jsonObject.getValue(option.getOrThrow<Snowflake>("value").string).jsonObject))
            CommandOption.OptionType.MENTIONABLE -> when {
                resolved["users"] != null && resolved["users"]!!.jsonObject.contains(option.getOrThrow<Snowflake>("value").string) -> Interaction.InteractionOption(name, type, User(client, resolved.getValue("users").jsonObject.getValue(option.getOrThrow<Snowflake>("value").string).jsonObject))
                resolved["roles"] != null && resolved["roles"]!!.jsonObject.contains(option.getOrThrow<Snowflake>("value").string) -> Interaction.InteractionOption(name, type, Role(guild!!, resolved.getValue("roles").jsonObject.getValue(option.getOrThrow<Snowflake>("value").string).jsonObject))
                else -> throw IllegalStateException()
            }
            CommandOption.OptionType.NUMBER -> Interaction.InteractionOption(name, type, option.getOrThrow<Double>("value"))
            else -> throw IllegalStateException()
        }
    }


}