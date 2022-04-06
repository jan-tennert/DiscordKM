/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.interactions.commands

import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.SerializableEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import io.github.jan.discordkm.internal.utils.get
import io.github.jan.discordkm.internal.utils.getId
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.int
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

open class ApplicationCommand(override val client: Client, override val data: JsonObject) : SerializableEntity, SnowflakeEntity, Nameable {

    /**
     * The type of the application command
     */
    val type: ApplicationCommandType
        get() = ApplicationCommandType[data["type", true]?.int ?: 1]

    override val id: Snowflake
        get() = data.getId()

    /**
     * The id of the application which created this application command
     */
    val applicationId: Snowflake
        get() = data.getOrThrow("application_id")

    /**
     * The id of the guild where this command was created in. Can be null if it's a global command
     */
    val guildId: Snowflake?
        get() = data.getOrNull<Snowflake>("guild_id")

    val guild: Guild?
        get() = client.guilds[guildId ?: Snowflake(0)]

    override val name: String
        get() = data.getOrThrow<String>("name")

    /**
     * The description of the command
     */
    val description: String
        get() = data.getOrThrow<String>("description")

    /**
     * The version of the command
     */
    val version: Snowflake
        get() = data.getOrThrow<Snowflake>("version")

    override fun toString(): String = "ApplicationCommand(id=$id, name=$name, guildId=${guild?.id}, type=$type)"
    override fun hashCode() = id.hashCode()
    override fun equals(other: Any?): Boolean = other is ApplicationCommand && other.id == id

}

class ChatInputCommand(client: Client, data: JsonObject) : ApplicationCommand(client, data) {

    /**
     * The options of the command
     */
    val options = data["options"]?.jsonArray?.map { Json { ignoreUnknownKeys = true }.decodeFromJsonElement<CommandOption>(it.jsonObject) } ?: emptyList()

}

enum class ApplicationCommandType : EnumWithValue<Int>{
    /**
     * These are the "slash commands"
     */
    CHAT_INPUT,

    /**
     * These pop out when you right-click a user
     */
    USER,

    /**
     * These pop out when you right-click a message
     */
    MESSAGE;

    override val value: Int
        get() = ordinal + 1

    companion object : EnumWithValueGetter<ApplicationCommandType, Int>(values())
}