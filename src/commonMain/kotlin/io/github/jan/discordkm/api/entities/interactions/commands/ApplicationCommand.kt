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
import io.github.jan.discordkm.internal.utils.getId
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.valueOfIndexOrDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

open class ApplicationCommand(override val client: Client, override val data: JsonObject) : SerializableEntity, SnowflakeEntity, Nameable {

    val type: ApplicationCommandType
        get() = valueOfIndexOrDefault(data.getOrThrow<Int>("type"), default = ApplicationCommandType.CHAT_INPUT)

    override val id: Snowflake
        get() = data.getId()

    val applicationId: Snowflake
        get() = data.getOrThrow("application_id")

    val guildId: Snowflake?
        get() = data.getOrNull<Snowflake>("guild_id")

    val guild: Guild?
        get() = client.guilds[guildId ?: Snowflake.empty()]

    override val name: String
        get() = data.getOrThrow<String>("name")

    val description: String
        get() = data.getOrThrow<String>("description")

    //options

    val version: Snowflake
        get() = data.getOrThrow<Snowflake>("version")

}

class ChatInputCommand(client: Client, data: JsonObject) : ApplicationCommand(client, data) {

    val options = data.getValue("options").jsonArray.map { Json.decodeFromJsonElement<CommandOption>(it.jsonObject) }

}

enum class ApplicationCommandType {
    CHAT_INPUT,
    USER,
    MESSAGE
}