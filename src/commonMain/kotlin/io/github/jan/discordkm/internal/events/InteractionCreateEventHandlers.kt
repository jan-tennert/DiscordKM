/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.containers.OptionContainer
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.api.entities.interactions.AutoCompleteInteraction
import io.github.jan.discordkm.api.entities.interactions.ComponentInteraction
import io.github.jan.discordkm.api.entities.interactions.InteractionOption
import io.github.jan.discordkm.api.entities.interactions.InteractionType
import io.github.jan.discordkm.api.entities.interactions.StandardInteraction
import io.github.jan.discordkm.api.entities.interactions.commands.ApplicationCommandType
import io.github.jan.discordkm.api.entities.interactions.commands.CommandOption
import io.github.jan.discordkm.api.entities.interactions.components.ComponentType
import io.github.jan.discordkm.api.entities.interactions.components.SelectOption
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.api.events.AutoCompleteEvent
import io.github.jan.discordkm.api.events.ButtonClickEvent
import io.github.jan.discordkm.api.events.InteractionCreateEvent
import io.github.jan.discordkm.api.events.MessageCommandEvent
import io.github.jan.discordkm.api.events.SelectionMenuEvent
import io.github.jan.discordkm.api.events.SlashCommandEvent
import io.github.jan.discordkm.api.events.UserCommandEvent
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.valueOfIndex
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class InteractionCreateEventHandler(val client: Client) : InternalEventHandler<InteractionCreateEvent> {

    override suspend fun handle(data: JsonObject) = when(InteractionType[data["type"]!!.int]) {
        InteractionType.PING -> TODO()
        InteractionType.APPLICATION_COMMAND -> extractApplicationCommand(data)
        InteractionType.MESSAGE_COMPONENT -> extractMessageComponent(data)
        InteractionType.APPLICATION_COMMAND_AUTOCOMPLETE -> extractCommandAutoComplete(data)
        else -> TODO()
    }

    private fun extractCommandAutoComplete(data: JsonObject) : InteractionCreateEvent {
        val interactionData = data.getValue("data").jsonObject
        val commandId = interactionData.getOrThrow<Snowflake>("id")
        val commandName = interactionData.getOrThrow<String>("name")
        var option = interactionData.getValue("options").jsonArray[0].jsonObject
        var subCommandGroup: String? = null
        var subCommand: String? = null
        val type = valueOfIndex<CommandOption.OptionType>(option.getOrThrow("type"), 1)
        if(type == CommandOption.OptionType.SUB_COMMAND_GROUP) {
            subCommandGroup = option.getOrThrow("name")
            subCommand = option.getValue("options").jsonArray[0].jsonObject.getOrThrow("name")
            option = option.getValue("options").jsonArray[0].jsonObject.getValue("options").jsonArray[0].jsonObject
        } else if(type == CommandOption.OptionType.SUB_COMMAND) {
            subCommand = option.getOrThrow("name")
            option = option.getValue("options").jsonArray[0].jsonObject
        }
        val focused = option.getOrNull<Boolean>("focused") ?: false
        val optionName = option.getOrThrow<String>("name")
        val optionValue = option.getOrNull<JsonPrimitive>("value")
        return when(type) {
            CommandOption.OptionType.STRING -> AutoCompleteEvent(client, AutoCompleteInteraction(client, data), commandName, commandId, optionName, optionValue?.contentOrNull, focused, subCommand, subCommandGroup)
            CommandOption.OptionType.INTEGER -> AutoCompleteEvent(client, AutoCompleteInteraction(client, data), commandName, commandId, optionName, optionValue?.intOrNull, focused, subCommand, subCommandGroup)
            CommandOption.OptionType.NUMBER -> AutoCompleteEvent(client, AutoCompleteInteraction(client, data), commandName, commandId, optionName, optionValue?.doubleOrNull, focused, subCommand, subCommandGroup)
            else -> throw IllegalStateException("Invalid autocomplete option type: $type")
        }
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
        val messageObject = data.getValue("data").jsonObject.getValue("resolved").jsonObject.getValue("messages").jsonObject.getValue(target).jsonObject
        val message = Message(messageObject, client)
        return MessageCommandEvent(client, interaction, name, message)
    }

    private fun extractUserCommand(data: JsonObject) : UserCommandEvent {
        val target = data.getValue("data").jsonObject.getOrThrow<String>("target_id")
        val interaction = StandardInteraction(client, data)
        val name = data.getValue("data").jsonObject.getOrThrow<String>("name")
        val userObject = data.getValue("data").jsonObject.getValue("resolved").jsonObject.jsonObject.getValue("users").jsonObject.getValue(target).jsonObject
        val user = User(userObject, client)
        return UserCommandEvent(client, interaction, name, user)
    }

    private fun extractChatInputCommand(data: JsonObject) : InteractionCreateEvent {
        var subCommandGroup: String? = null
        var subCommand: String? = null
        val resolved = data.getValue("data").jsonObject["resolved"]?.jsonObject
        val name = data.getValue("data").jsonObject.getOrThrow<String>("name")
        val options = mutableListOf<InteractionOption>()
        if(data.getValue("data").jsonObject["options"] != null) {
            data.getValue("data").jsonObject.getValue("options").jsonArray.forEach { option ->
                option as JsonObject
                when(CommandOption.OptionType[option["type"]!!.int]) {
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
        }
        return SlashCommandEvent(client, StandardInteraction(client, data), name, OptionContainer(options), subCommand, subCommandGroup)
    }

    private fun extractOption(resolved: JsonObject?, option: JsonObject, fullData: JsonObject) : InteractionOption {
        val type = CommandOption.OptionType.values().first { it.ordinal + 1 == option.jsonObject.getOrThrow<Int>("type") }
        val name = option.getOrThrow<String>("name")
        val guild = client.guilds[fullData.getOrThrow<Snowflake>(
            "guild_id"
        )]
        return when(type) {
            CommandOption.OptionType.STRING -> InteractionOption(name, type, option.getOrThrow<String>("value"))
            CommandOption.OptionType.INTEGER -> InteractionOption(name, type, option.getOrThrow<Int>("value"))
            CommandOption.OptionType.BOOLEAN -> InteractionOption(name, type, option.getOrThrow<Boolean>("value"))
            CommandOption.OptionType.USER -> InteractionOption(name, type, User(resolved!!.getValue("users").jsonObject.getValue(option.getOrThrow<Snowflake>("value").string).jsonObject, client))
            CommandOption.OptionType.CHANNEL -> InteractionOption(name, type, ChannelSerializer.deserialize(resolved!!.getValue("channels").jsonObject.getValue(option.getOrThrow<Snowflake>("value").string).jsonObject, guild!!))
            CommandOption.OptionType.ROLE -> InteractionOption(name, type, Role(resolved!!.getValue("roles").jsonObject.getValue(option.getOrThrow<Snowflake>("value").string).jsonObject, guild!!))
            CommandOption.OptionType.MENTIONABLE -> when {
                resolved!!["users"] != null && resolved["users"]!!.jsonObject.contains(option.getOrThrow<Snowflake>("value").string) -> InteractionOption(name, type, User(resolved.getValue("users").jsonObject.getValue(option.getOrThrow<Snowflake>("value").string).jsonObject, client))
                resolved["roles"] != null && resolved["roles"]!!.jsonObject.contains(option.getOrThrow<Snowflake>("value").string) -> InteractionOption(name, type, Role(resolved.getValue("roles").jsonObject.getValue(option.getOrThrow<Snowflake>("value").string).jsonObject, guild!!))
                else -> throw IllegalStateException()
            }
            CommandOption.OptionType.NUMBER -> InteractionOption(name, type, option.getOrThrow<Double>("value"))
            else -> throw IllegalStateException()
        }
    }


}