/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.lists

import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.Snowflake

sealed interface DiscordList <K, V> : Iterable<V> {

    val internalMap: Map<K, V>
    val size: Int
        get() = internalMap.size

    operator fun contains(other: K) = other in internalMap

    /**
     * Gets this object from the cache
     */
    operator fun get(key: K) = internalMap[key]

    override operator fun iterator() = internalMap.values.iterator()

}

sealed interface SnowflakeList <V> : DiscordList<Snowflake, V>

sealed interface NameList <K, V : Nameable> : DiscordList<K, V> {

    operator fun get(name: String) = internalMap.filter { it.value.name == name }.values

}

sealed interface NameableSnowflakeList <V : Nameable> : SnowflakeList<V>, NameList<Snowflake, V>