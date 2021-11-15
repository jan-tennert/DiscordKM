/**
 * DiscordKM is a kot  lin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.utils

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.ISO8601
import com.soywiz.klock.parse
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.invites.Invite
import io.github.jan.discordkm.api.entities.guild.invites.InviteApplication
import io.github.jan.discordkm.api.entities.interactions.commands.ApplicationCommand
import io.github.jan.discordkm.api.entities.interactions.commands.ChatInputCommand
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put

fun JsonObject.getId(): Snowflake {
    return Snowflake.fromId(getOrThrow<String>("id"))
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
        UInt::class -> getValue(key).jsonPrimitive.int.toUInt() as T
        JsonPrimitive::class -> getValue(key).jsonPrimitive as T
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

inline fun JsonObject.extractApplicationCommand(client: Client) = when((getOrNull<Int>("type") ?: 1)) {
    1 -> ChatInputCommand(client, this)
    else -> ApplicationCommand(client, this)
}

inline fun <reified T>JsonObject.extractClientEntity(client: Client) = when(T::class) {
    Invite::class -> Invite(client, this) as T
    InviteApplication::class -> InviteApplication(client, this) as T
    else -> throw IllegalStateException()
}




fun <V>JsonObjectBuilder.putOptional(key: String, value: V?) { value?.let { put(key, value.toString()) }}

fun JsonObjectBuilder.putJsonObject(json: JsonObject) = json.forEach { (key, value) -> put(key, value) }


val JsonElement.boolean : Boolean get() = jsonPrimitive.boolean
val JsonElement.int : Int get() = jsonPrimitive.int
val JsonElement.long : Long get() = jsonPrimitive.long
val JsonElement.double : Double get() = jsonPrimitive.double
val JsonElement.string : String get() = jsonPrimitive.contentOrNull ?: ""
val JsonElement.snowflake : Snowflake get() = Snowflake.fromId(string)
val JsonElement.isoTimestamp : DateTimeTz get() = ISO8601.DATETIME_UTC_COMPLETE.parse(string)

operator fun JsonObject.get(key: String, nonNull: Boolean): JsonElement? = this[key]?.let { it -> if(nonNull && it is JsonPrimitive && it.jsonPrimitive.content != "null") it else null }


