/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.restaction

class QueryParameterBuilder {

    private val map = hashMapOf<String, String>()

    fun build() : String {
        if(map.isEmpty()) return ""
        var isFirst: Boolean = true
        var index = 1
        val query = buildString {
            map.forEach { (key, value)  ->
                val prefix = if(isFirst) "?" else "&"
                append("$prefix$key=$value")
                index++
                isFirst = false
            }
        }
        return query
    }

    fun put(key: String, value: Any) {
        map[key] = value.toString()
    }

    fun putOptional(key: String, value: Any?) {
        value?.let {
            map[key] = value.toString()
        }
    }

}

fun buildQuery(builder: QueryParameterBuilder.() -> Unit) = QueryParameterBuilder().apply(builder).build()