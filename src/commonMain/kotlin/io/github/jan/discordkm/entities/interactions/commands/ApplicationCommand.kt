/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.interactions.commands

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.SerializableEntity
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.SnowflakeEntity
import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.utils.getId
import io.github.jan.discordkm.utils.getOrNull
import io.github.jan.discordkm.utils.getOrThrow
import kotlinx.serialization.json.JsonObject

class ApplicationCommand(override val client: Client, override val data: JsonObject) : SerializableEntity, SnowflakeEntity {

    val type: ApplicationCommandType
        get() = ApplicationCommandType.values().firstOrNull { it.ordinal == data.getOrThrow<Int>("type") } ?: ApplicationCommandType.CHAT_INPUT

    override val id: Snowflake
        get() = data.getId()

    val applicationId: Snowflake
        get() = data.getOrThrow("application_id")

    val guildId: Snowflake?
        get() = data.getOrNull<Snowflake>("guild_id")

    val guild: Guild?
        get() = client.guilds[guildId ?: Snowflake.empty()]

    val name: String
        get() = data.getOrThrow<String>("name")

    val description: String
        get() = data.getOrThrow<String>("description")

    //options

    val version: Snowflake
        get() = data.getOrThrow<Snowflake>("version")

}

enum class ApplicationCommandType {
    CHAT_INPUT,
    USER,
    MESSAGE
}