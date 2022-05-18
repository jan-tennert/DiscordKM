package io.github.jan.discordkm.internal.serialization.serializers.channel

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.interactions.commands.ApplicationCommandCacheEntry
import io.github.jan.discordkm.api.entities.interactions.commands.ApplicationCommandCacheEntryImpl
import io.github.jan.discordkm.api.entities.interactions.commands.ApplicationCommandType
import io.github.jan.discordkm.api.entities.interactions.commands.ChatInputCommandCacheEntryImpl
import io.github.jan.discordkm.api.entities.interactions.commands.CommandOption
import io.github.jan.discordkm.internal.serialization.BaseEntitySerializer
import io.github.jan.discordkm.internal.utils.boolean
import io.github.jan.discordkm.internal.utils.get
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.localeMap
import io.github.jan.discordkm.internal.utils.long
import io.github.jan.discordkm.internal.utils.snowflake
import io.github.jan.discordkm.internal.utils.string
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray

private val json = Json { ignoreUnknownKeys = true }

object ApplicationCommandSerializer {

    fun deserialize(data: JsonObject, client: DiscordClient): ApplicationCommandCacheEntry {
        return when(val type = ApplicationCommandType[data["type"]!!.int]) {
            ApplicationCommandType.CHAT_INPUT -> ChatInputCommandCacheEntryImpl(
                data["id"]!!.snowflake,
                data["name"]!!.string,
                type,
                data["application_id"]!!.snowflake,
                data["guild_id"]?.snowflake?.let { Guild(it, client) },
                data["description"]!!.string,
                data["dm_permission", true]?.boolean ?: false,
                data["default_member_permissions", true]?.let {
                    Permission.decode(it.long)
                } ?: emptySet(),
                data["description_localizations", true]?.localeMap ?: emptyMap(),
                data["name_localizations", true]?.localeMap ?: emptyMap(),
                data["version", true]?.snowflake ?: Snowflake(0),
                data["options"]?.let {
                    json.decodeFromJsonElement<List<CommandOption>>(it.jsonArray)
                } ?: emptyList()
            )
            else -> ApplicationCommandCacheEntryImpl(
                data["id"]!!.snowflake,
                data["name"]!!.string,
                type,
                data["application_id"]!!.snowflake,
                data["guild_id"]?.snowflake?.let { Guild(it, client) },
                data["description"]!!.string,
                data["dm_permission", true]?.boolean ?: false,
                data["default_member_permissions", true]?.let {
                    Permission.decode(it.long)
                } ?: emptySet(),
                data["description_localizations", true]?.localeMap ?: emptyMap(),
                data["name_localizations", true]?.localeMap ?: emptyMap(),
                data["version", true]?.snowflake ?: Snowflake(0),
            )
        }
    }

}