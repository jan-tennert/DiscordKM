/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity

sealed interface BaseContainer <K, T> : Iterable<T> {

    val values: Collection<T>

    operator fun get(key: K): T?

    override fun iterator() = values.iterator()

}

interface SnowflakeContainer <T : SnowflakeEntity> : BaseContainer<Snowflake, T> {

    override fun get(key: Snowflake) = values.firstOrNull { it.id == key }

}

interface NameableContainer<K, T : Nameable> : BaseContainer<K, T> {

    operator fun get(key: String, ignoreCase: Boolean = false) = values.filter { if(ignoreCase) it.name.lowercase() == key.lowercase() else it.name == key }

}

interface NameableSnowflakeContainer <T> : SnowflakeContainer<T>, NameableContainer<Snowflake, T> where T : SnowflakeEntity, T : Nameable