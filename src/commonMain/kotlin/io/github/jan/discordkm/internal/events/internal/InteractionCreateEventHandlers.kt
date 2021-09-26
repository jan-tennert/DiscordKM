package io.github.jan.discordkm.internal.events.internal

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.interactions.AutoCompleteInteraction
import io.github.jan.discordkm.api.entities.interactions.ComponentInteraction
import io.github.jan.discordkm.api.entities.interactions.Interaction
import io.github.jan.discordkm.api.entities.interactions.StandardInteraction
import io.github.jan.discordkm.api.entities.interactions.commands.ApplicationCommandType
import io.github.jan.discordkm.api.entities.interactions.commands.CommandOption
import io.github.jan.discordkm.api.entities.interactions.components.ComponentType
import io.github.jan.discordkm.api.entities.interactions.components.SelectOption
import io.github.jan.discordkm.api.entities.lists.OptionList
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.api.events.AutoCompleteEvent
import io.github.jan.discordkm.api.events.ButtonClickEvent
import io.github.jan.discordkm.api.events.InteractionCreateEvent
import io.github.jan.discordkm.api.events.MessageCommandEvent
import io.github.jan.discordkm.api.events.SelectionMenuEvent
import io.github.jan.discordkm.api.events.SlashCommandEvent
import io.github.jan.discordkm.api.events.UserCommandEvent
import io.github.jan.discordkm.internal.entities.UserData
import io.github.jan.discordkm.internal.entities.guilds.RoleData
import io.github.jan.discordkm.internal.utils.extractChannel
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.valueOfIndex
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class InteractionCreateEventHandler(val client: Client) : InternalEventHandler<InteractionCreateEvent> {

    override fun handle(data: JsonObject) = when(Interaction.InteractionType.values().first { it.ordinal + 1 == data.getOrThrow<Int>("type") }) {
        Interaction.InteractionType.PING -> TODO()
        Interaction.InteractionType.APPLICATION_COMMAND -> extractApplicationCommand(data)
        Interaction.InteractionType.MESSAGE_COMPONENT -> extractMessageComponent(data)
        Interaction.InteractionType.APPLICATION_COMMAND_AUTOCOMPLETE -> extractCommandAutoComplete(data)
    }

    private fun extractCommandAutoComplete(data: JsonObject) : InteractionCreateEvent {
        val interactionData = data.getValue("data").jsonObject
        val commandId = interactionData.getOrThrow<Snowflake>("id")
        val commandName = interactionData.getOrThrow<String>("name")
        val option = interactionData.getValue("options").jsonArray[0].jsonObject
        val focused = option.getOrThrow<Boolean>("focused")
        val optionName = option.getOrThrow<String>("name")
        val optionValue = option.getOrThrow<String>("value")
        return AutoCompleteEvent(client, AutoCompleteInteraction(client, data), commandName, commandId, optionName, optionValue, focused)
    }

    private fun extractMessageComponent(data: JsonObject) : InteractionCreateEvent {
        val interactionData = data.getValue("data").jsonObject
        return when(valueOfIndex<ComponentType>(interactionData.getOrThrow("component_type"), 1)) {
            ComponentType.BUTTON -> ButtonClickEvent(client, ComponentInteraction(client, data), interactionData.getOrThrow("custom_id"))
            ComponentType.SELECTION_MENU -> SelectionMenuEvent(client, ComponentInteraction(client, data), interactionData.getValue("values").jsonArray.map { SelectOption(value = it.jsonPrimitive.content) }, interactionData.getOrThrow("custom_id"))
            else -> throw IllegalStateException("Component type not supported")
        }
    }

    private fun extractApplicationCommand(data: JsonObject) = when(ApplicationCommandType.values().first { it.ordinal + 1 == data.getValue("data").jsonObject.getOrThrow<Int>("type") }) {
        ApplicationCommandType.CHAT_INPUT -> extractChatInputCommand(data)
        ApplicationCommandType.USER -> extractUserCommand(data)
        ApplicationCommandType.MESSAGE -> extractMessageCommand(data)
    }

    private fun extractMessageCommand(data: JsonObject) : MessageCommandEvent {
        val target = data.getValue("data").jsonObject.getOrThrow<String>("target_id")
        val interaction = StandardInteraction(client, data)
        val name = data.getValue("data").jsonObject.getOrThrow<String>("name")
        val message = Message(interaction.channel!!, data.getValue("data").jsonObject.getValue("resolved").jsonObject.getValue("messages").jsonObject.getValue(target).jsonObject)
        return MessageCommandEvent(client, interaction, name, message)
    }

    private fun extractUserCommand(data: JsonObject) : UserCommandEvent {
        val target = data.getValue("data").jsonObject.getOrThrow<String>("target_id")
        val interaction = StandardInteraction(client, data)
        val name = data.getValue("data").jsonObject.getOrThrow<String>("name")
        val user = UserData(client, data.getValue("data").jsonObject.getValue("resolved").jsonObject.jsonObject.getValue("users").jsonObject.getValue(target).jsonObject)
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
            when(valueOfIndex<CommandOption.OptionType>(option.getOrThrow("type"), 1)) {
                CommandOption.OptionType.SUB_COMMAND -> {
                    subCommand = option.jsonObject.getOrThrow<String>("name")
                    option["options"]?.jsonArray?.forEach {
                        options += extractOption(resolved, it.jsonObject, data)
                    }
                }
                CommandOption.OptionType.SUB_COMMAND_GROUP -> {
                    subCommandGroup = option.jsonObject.getOrThrow<String>("name")
                    subCommand = option.getValue("options").jsonArray[0].jsonObject.getOrThrow<String>("name")
                    option.getValue("options").jsonArray[0].jsonObject["options"]?.jsonArray?.forEach {
                        options += extractOption(resolved, it.jsonObject, data)
                    }
                }
                else -> options += extractOption(resolved, option.jsonObject, data)
            }
        }
        return SlashCommandEvent(client, StandardInteraction(client, data), name, OptionList(options), subCommand, subCommandGroup)
    }

    private fun extractOption(resolved: JsonObject?, option: JsonObject, fullData: JsonObject) : Interaction.InteractionOption {
        val type = CommandOption.OptionType.values().first { it.ordinal + 1 == option.jsonObject.getOrThrow<Int>("type") }
        val name = option.getOrThrow<String>("name")
        val guild = client.guilds[fullData.getOrThrow<Snowflake>(
            "guild_id"
        )]
        return when(type) {
            CommandOption.OptionType.STRING -> Interaction.InteractionOption(name, type, option.getOrThrow<String>("value"))
            CommandOption.OptionType.INTEGER -> Interaction.InteractionOption(name, type, option.getOrThrow<Int>("value"))
            CommandOption.OptionType.BOOLEAN -> Interaction.InteractionOption(name, type, option.getOrThrow<Boolean>("value"))
            CommandOption.OptionType.USER -> Interaction.InteractionOption(name, type, UserData(client, resolved!!.getValue("users").jsonObject.getValue(option.getOrThrow<Snowflake>("value").string).jsonObject))
            CommandOption.OptionType.CHANNEL -> Interaction.InteractionOption(name, type, resolved!!.getValue("channels").jsonObject.getValue(option.getOrThrow<Snowflake>("value").string).jsonObject.extractChannel(client, guild))
            CommandOption.OptionType.ROLE -> Interaction.InteractionOption(name, type, RoleData(guild!!, resolved!!.getValue("roles").jsonObject.getValue(option.getOrThrow<Snowflake>("value").string).jsonObject))
            CommandOption.OptionType.MENTIONABLE -> when {
                resolved!!["users"] != null && resolved["users"]!!.jsonObject.contains(option.getOrThrow<Snowflake>("value").string) -> Interaction.InteractionOption(name, type, UserData(client, resolved.getValue("users").jsonObject.getValue(option.getOrThrow<Snowflake>("value").string).jsonObject))
                resolved["roles"] != null && resolved["roles"]!!.jsonObject.contains(option.getOrThrow<Snowflake>("value").string) -> Interaction.InteractionOption(name, type, RoleData(guild!!, resolved.getValue("roles").jsonObject.getValue(option.getOrThrow<Snowflake>("value").string).jsonObject))
                else -> throw IllegalStateException()
            }
            CommandOption.OptionType.NUMBER -> Interaction.InteractionOption(name, type, option.getOrThrow<Double>("value"))
            else -> throw IllegalStateException()
        }
    }


}