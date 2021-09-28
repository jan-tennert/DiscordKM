/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal

import co.touchlab.stately.collections.IsoMutableMap
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity

class Cache<T>(internal var internalMap: IsoMutableMap<Snowflake, T> = IsoMutableMap()) {

    val values: List<T>
        get() = internalMap.values.toList()

    @PublishedApi
    internal operator fun set(id: Snowflake, value: T) {
        internalMap[id] = value
    }

    @PublishedApi
    internal fun remove(id: Snowflake) {
        internalMap.remove(id)
    }

    companion object {

        fun <S : SnowflakeEntity> fromSnowflakeEntityList(list: List<S>) = Cache(IsoMutableMap { mutableMapOf<Snowflake, S>().apply { putAll(list.associateBy { it.id }) } })

    }

}