/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.utils

import io.github.jan.discordkm.api.entities.EnumSerializer
import io.github.jan.discordkm.api.entities.SerializableEnum
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.api.entities.guild.Sticker
import io.github.jan.discordkm.api.entities.guild.channels.Category
import io.github.jan.discordkm.api.entities.guild.channels.NewsChannel
import io.github.jan.discordkm.api.entities.guild.channels.StageChannel
import io.github.jan.discordkm.api.entities.guild.channels.TextChannel
import io.github.jan.discordkm.api.entities.guild.channels.Thread
import io.github.jan.discordkm.api.entities.guild.channels.VoiceChannel
import io.github.jan.discordkm.api.entities.guild.invites.Invite
import io.github.jan.discordkm.api.entities.guild.invites.InviteApplication
import io.github.jan.discordkm.api.entities.interactions.commands.ApplicationCommand
import io.github.jan.discordkm.api.entities.interactions.commands.ChatInputCommand
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.api.entities.misc.Color
import io.github.jan.discordkm.api.entities.misc.EnumList
import io.github.jan.discordkm.internal.entities.UserData
import io.github.jan.discordkm.internal.entities.channels.Channel
import io.github.jan.discordkm.internal.entities.channels.ChannelType
import io.github.jan.discordkm.internal.entities.channels.MessageChannel
import io.github.jan.discordkm.internal.entities.channels.PrivateChannel
import io.github.jan.discordkm.internal.entities.channels.PrivateChannelData
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.entities.guilds.MemberData
import io.github.jan.discordkm.internal.entities.guilds.RoleData
import io.github.jan.discordkm.internal.entities.guilds.channels.CategoryData
import io.github.jan.discordkm.internal.entities.guilds.channels.NewsChannelData
import io.github.jan.discordkm.internal.entities.guilds.channels.StageChannelData
import io.github.jan.discordkm.internal.entities.guilds.channels.TextChannelData
import io.github.jan.discordkm.internal.entities.guilds.channels.ThreadData
import io.github.jan.discordkm.internal.entities.guilds.channels.VoiceChannelData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

fun JsonObject.getId(): Snowflake {
    return Snowflake.fromId(getOrThrow<String>("id"))
}
fun JsonObject.getColor(key: String) = Color(getValue(key).jsonPrimitive.int)
fun <T : SerializableEnum<T>> JsonObject.getEnums(key: String, serializer: EnumSerializer<T>) = if(get(key)?.jsonPrimitive?.long != null) serializer.decode(get(key)?.jsonPrimitive?.long!!) else EnumList.empty()
fun JsonObject.getRoleTag(key: String): Role.Tag? {
    val json = get("tags")?.jsonObject
    return json?.let { Role.Tag(it["bot_id"]?.jsonPrimitive?.longOrNull, it.jsonObject["integration_id"]?.jsonPrimitive?.longOrNull, it.jsonObject["premium_subscriber"]?.jsonPrimitive?.booleanOrNull) }
}

fun String.toJsonObject() = Json.decodeFromString<JsonObject>(this)
fun String.toJsonArray() = Json.decodeFromString<JsonArray>(this)

inline fun <reified T> JsonObject.getOrThrow(key: String): T {
    if(getValue(key).toString() == "null") throw NoSuchElementException()
    return when(T::class) {
        String::class -> try {
            getValue(key).jsonPrimitive.content
        } catch(_: Exception) { getValue(key).toString() } as T
        Int::class -> getValue(key).jsonPrimitive.int as T
        Long::class -> getValue(key).jsonPrimitive.long as T
        Double::class -> getValue(key).jsonPrimitive.double as T
        Boolean::class -> getValue(key).jsonPrimitive.boolean as T
        Snowflake::class -> Snowflake.fromId(getValue(key).jsonPrimitive.content) as T
        else -> throw IllegalStateException()
    }
}

inline fun <reified T> JsonObject.getOrDefault(key: String, default: T) = try {
    getOrThrow<T>(key)
} catch(_: NoSuchElementException) {
    default
}

inline fun <reified T> JsonObject.getOrNull(key: String) = try {
    getOrThrow<T>(key)
} catch(_: NoSuchElementException) {
    null
}

inline fun <reified T>JsonObject.extractGuildEntity(guild: Guild) = when(T::class) {
    Guild.WelcomeScreen::class -> Guild.WelcomeScreen(guild, this) as T
    Member::class -> MemberData(guild, this) as T
    Role::class -> RoleData(guild, this) as T
    VoiceChannel::class -> VoiceChannelData(guild, this) as T
    StageChannel::class -> StageChannelData(guild, this) as T
    NewsChannel::class -> NewsChannelData(guild, this) as T
    Category::class -> CategoryData(guild, this) as T
    TextChannel::class -> TextChannelData(guild, this) as T
    Thread::class -> ThreadData(guild, this) as T
    Thread.ThreadMember::class -> Thread.ThreadMember(guild, this) as T
    else -> {
        throw IllegalStateException()
    }
}

inline fun JsonObject.extractApplicationCommand(client: Client) = when((getOrNull<Int>("type") ?: 1)) {
    1 -> ChatInputCommand(client, this)
    else -> ApplicationCommand(client, this)
}

inline fun <reified T>JsonObject.extractClientEntity(client: Client) = when(T::class) {
    PrivateChannel::class -> PrivateChannelData(client, this) as T
    User::class -> UserData(client, this) as T
    Guild::class -> GuildData(client, this) as T
    Emoji.Emote::class -> Emoji.Emote(this, client) as T
    Sticker::class -> Sticker(this, client) as T
    Invite::class -> Invite(client, this) as T
    InviteApplication::class -> InviteApplication(client, this) as T
    else -> throw IllegalStateException()
}

inline fun <reified T>JsonObject.extractMessageChannelEntity(channel: MessageChannel) = when(T::class) {
    Message::class -> Message(channel, this) as T
    else -> throw IllegalStateException()
}

inline fun JsonObject.extractChannel(client: Client, guild: Guild? = null): Channel = when(ChannelType.values().first { it.id == getValue("type").jsonPrimitive.int }) {
    ChannelType.GUILD_TEXT -> extractGuildEntity<TextChannel>(guild!!)
    ChannelType.GUILD_VOICE -> extractGuildEntity<VoiceChannel>(guild!!)
    ChannelType.GUILD_CATEGORY -> extractGuildEntity<Category>(guild!!)
    ChannelType.DM -> extractClientEntity<PrivateChannel>(client)
    ChannelType.GUILD_NEWS -> extractGuildEntity<NewsChannel>(guild!!)
    ChannelType.GUILD_STAGE_VOICE -> extractGuildEntity<StageChannel>(guild!!)
    else -> throw IllegalStateException()
}

fun <V>JsonObjectBuilder.putOptional(key: String, value: V?) { value?.let { put(key, value.toString()) }}

fun JsonObjectBuilder.putJsonObject(json: JsonObject) = json.forEach { (key, value) -> put(key, value) }
