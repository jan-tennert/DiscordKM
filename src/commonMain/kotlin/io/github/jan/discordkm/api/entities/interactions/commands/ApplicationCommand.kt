/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.interactions.commands

import io.github.jan.discordkm.api.entities.DiscordLocale
import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.SerializableEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.internal.DiscordKMUnstable
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import io.github.jan.discordkm.internal.utils.boolean
import io.github.jan.discordkm.internal.utils.get
import io.github.jan.discordkm.internal.utils.getId
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.long
import io.github.jan.discordkm.internal.utils.string
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

sealed interface ApplicationCommandCacheEntry : SnowflakeEntity, Nameable {


    val type: ApplicationCommandType
    val applicationId: Snowflake
    val guild: Guild?
    val description: String
    val enabledInDMs: Boolean
    val defaultMemberPermissions: Set<Permission>
    val descriptionLocalizations: Map<String, DiscordLocale>
    val nameLocalizations: Map<String, DiscordLocale>
    val version: Snowflake

}

sealed interface ChatInputCommandCacheEntry : ApplicationCommandCacheEntry {

    val option: List<CommandOption>

}

internal class ApplicationCommandCacheEntryImpl(
    override val id: Snowflake,
    override val name: String,
    override val type: ApplicationCommandType,
    override val applicationId: Snowflake,
    override val guild: Guild?,
    override val description: String,
    override val enabledInDMs: Boolean,
    override val defaultMemberPermissions: Set<Permission>,
    override val descriptionLocalizations: Map<String, DiscordLocale>,
    override val nameLocalizations: Map<String, DiscordLocale>,
    override val version: Snowflake
) : ApplicationCommandCacheEntry {

    override fun hashCode() = id.hashCode()
    override fun equals(other: Any?) = other is ApplicationCommandCacheEntry && other.id == id
    override fun toString() = "ApplicationCommandCacheEntry(id=$id, name=$name, type=$type, description=$description, version=$version)"

}

internal class ChatInputCommandCacheEntryImpl(
    override val id: Snowflake,
    override val name: String,
    override val type: ApplicationCommandType,
    override val applicationId: Snowflake,
    override val guild: Guild?,
    override val description: String,
    override val enabledInDMs: Boolean,
    override val defaultMemberPermissions: Set<Permission>,
    override val descriptionLocalizations: Map<String, DiscordLocale>,
    override val nameLocalizations: Map<String, DiscordLocale>,
    override val version: Snowflake,
    override val option: List<CommandOption>
) : ChatInputCommandCacheEntry {

    override fun hashCode() = id.hashCode()
    override fun equals(other: Any?) = other is ApplicationCommandCacheEntry && other.id == id
    override fun toString() = "ChatInputCommandCacheEntry(id=$id, name=$name, type=$type, description=$description, version=$version)"

}

enum class ApplicationCommandType : EnumWithValue<Int>{
    /*
     * These are the "slash commands"
     */
    CHAT_INPUT,

    /*
     * These pop out when you right-click a user
     */
    USER,

    /*
     * These pop out when you right-click a message
     */
    MESSAGE;

    override val value: Int
        get() = ordinal + 1

    companion object : EnumWithValueGetter<ApplicationCommandType, Int>(values())
}