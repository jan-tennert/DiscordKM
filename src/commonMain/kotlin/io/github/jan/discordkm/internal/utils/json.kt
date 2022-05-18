/*
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
import io.github.jan.discordkm.api.entities.DiscordLocale
import io.github.jan.discordkm.api.entities.Snowflake
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put

fun JsonObject.getId(): Snowflake {
    return Snowflake(getOrThrow<String>("id"))
}
fun String.toJsonObject() = Json.decodeFromString<JsonObject>(this)
fun String.toJsonArray() = Json.decodeFromString<JsonArray>(this)

inline fun <reified V, T : EnumWithValue<V>> JsonObjectBuilder.put(key: String, value: T) {
    put(key, when(V::class) {
        Int::class -> JsonPrimitive(value.value as Int)
        Long::class -> JsonPrimitive(value.value as Long)
        Double::class -> JsonPrimitive(value.value as Double)
        Boolean::class -> JsonPrimitive(value.value as Boolean)
        String::class -> JsonPrimitive(value.value as String)
        else -> throw IllegalArgumentException("Can't put a ${V::class.simpleName} in a JsonObject")
    })
}

fun JsonObject.modify(builder: JsonObjectBuilder.() -> Unit) = buildJsonObject {
    this@modify.forEach { put(it.key, it.value) }
    builder(this)
}

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
        Snowflake::class -> Snowflake(getValue(key).jsonPrimitive.content) as T
        UInt::class -> getValue(key).jsonPrimitive.int.toUInt() as T
        JsonPrimitive::class -> getValue(key).jsonPrimitive as T
        else -> throw IllegalStateException()
    }
}

inline fun <reified T> JsonObject.getOrNull(key: String) = try {
    getOrThrow<T>(key)
} catch(_: NoSuchElementException) {
    null
}

fun <V>JsonObjectBuilder.putOptional(key: String, value: V?) { value?.let { put(key, value.toString()) }}
fun JsonObjectBuilder.putOptional(key: String, value: Boolean?) { value?.let { put(key, value) } }
fun JsonObjectBuilder.putOptional(key: String, value: String?) { value?.let { put(key, value) } }
fun JsonObjectBuilder.putOptional(key: String, value: Int?) { value?.let { put(key, value) } }
fun JsonObjectBuilder.putOptional(key: String, value: Long?) { value?.let { put(key, value) } }
fun JsonObjectBuilder.putOptional(key: String, value: Double?) { value?.let { put(key, value) } }
fun JsonObjectBuilder.putOptional(key: String, value: JsonElement?) { value?.let { put(key, value) } }

fun JsonObjectBuilder.putJsonObject(json: JsonObject) = json.forEach { (key, value) -> put(key, value) }

fun Iterable<JsonElement>.toJsonArray() = JsonArray(this.toList())

val JsonElement.boolean : Boolean get() = jsonPrimitive.boolean
val JsonElement.int : Int get() = jsonPrimitive.int
val JsonElement.long : Long get() = jsonPrimitive.long
val JsonElement.double : Double get() = jsonPrimitive.double
val JsonElement.string : String get() = jsonPrimitive.contentOrNull ?: ""
val JsonElement.snowflake : Snowflake get() = Snowflake(string)
val JsonElement.isoTimestamp : DateTimeTz get() = ISO8601.DATETIME_UTC_COMPLETE.parse(string)
val JsonElement.locale: DiscordLocale get() = DiscordLocale[string]
val JsonElement.localeMap: Map<String, DiscordLocale> get() = jsonObject.map { (key, v) -> key to v.locale }.toMap()

operator fun JsonObject.get(key: String, nonNull: Boolean): JsonElement? = this[key]?.let { it -> if(nonNull && it is JsonPrimitive && it.jsonPrimitive.content != "null") it else null }


