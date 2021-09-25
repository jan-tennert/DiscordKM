/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.lists

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity

sealed interface DiscordList <T : SnowflakeEntity> : Iterable<T> {

    val internalList: List<T>
    val size: Int
        get() = internalList.size

    operator fun contains(other: Snowflake) = internalList.any { it.id == other }

    /**
     * Gets this object from the cache
     */
    operator fun get(id: Snowflake) = internalList.firstOrNull { it.id == id }

    /**
     * Searches for the object in the cache and returns a list of matching objects
     */
    operator fun get(name: String) : List<T>

    override operator fun iterator() = internalList.iterator()

}