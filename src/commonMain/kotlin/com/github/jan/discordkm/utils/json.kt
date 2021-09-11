package com.github.jan.discordkm.utils

import com.github.jan.discordkm.Client
import com.github.jan.discordkm.entities.EnumSerializer
import com.github.jan.discordkm.entities.SerializableEnum
import com.github.jan.discordkm.entities.User
import com.github.jan.discordkm.entities.channels.PrivateChannel
import com.github.jan.discordkm.entities.guild.Emoji
import com.github.jan.discordkm.entities.guild.Guild
import com.github.jan.discordkm.entities.guild.Member
import com.github.jan.discordkm.entities.guild.Role
import com.github.jan.discordkm.entities.guild.Sticker
import com.github.jan.discordkm.entities.guild.channels.Category
import com.github.jan.discordkm.entities.guild.channels.NewsChannel
import com.github.jan.discordkm.entities.guild.channels.StageChannel
import com.github.jan.discordkm.entities.guild.channels.TextChannel
import com.github.jan.discordkm.entities.guild.channels.VoiceChannel
import com.github.jan.discordkm.entities.messages.Message
import com.github.jan.discordkm.entities.messages.MessageEmbed
import com.github.jan.discordkm.entities.misc.Color
import com.github.jan.discordkm.entities.misc.EnumList
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull

fun JsonObject.getId(): Long {
    return getValue("id").jsonPrimitive.content.toLong()
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
        String::class -> getValue(key).jsonPrimitive.content as T
        Int::class -> getValue(key).jsonPrimitive.int as T
        Long::class -> getValue(key).jsonPrimitive.long as T
        Double::class -> getValue(key).jsonPrimitive.double as T
        Boolean::class -> getValue(key).jsonPrimitive.boolean as T
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
    Member::class -> Member(guild, this) as T
    Role::class -> Role(guild, this) as T
    VoiceChannel::class -> VoiceChannel(guild, this) as T
    StageChannel::class -> StageChannel(guild, this) as T
    NewsChannel::class -> NewsChannel(guild, this) as T
    Category::class -> Category(guild, this) as T
    TextChannel::class -> TextChannel(guild, this) as T
    else -> throw IllegalStateException()
}

inline fun <reified T>JsonObject.extractClientEntity(client: Client) = when(T::class) {
    PrivateChannel::class -> PrivateChannel(client, this) as T
    Message::class -> Message(client, this) as T
    User::class -> User(client, this) as T
    Guild::class -> Guild(client, this) as T
    Emoji::class -> Emoji(this, client) as T
    Sticker::class -> Sticker(this, client) as T
    else -> throw IllegalStateException()
}

inline fun <reified T>JsonObject.extract() = when(T::class) {
    MessageEmbed::class -> Json.decodeFromString<MessageEmbed>(toString()) as T
    else -> throw IllegalStateException()
}