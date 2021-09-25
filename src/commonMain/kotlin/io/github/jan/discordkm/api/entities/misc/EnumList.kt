/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.misc

import io.github.jan.discordkm.api.entities.EnumSerializer
import io.github.jan.discordkm.api.entities.SerializableEnum

class EnumList<T : SerializableEnum<T>>(serializer: EnumSerializer<T>, private val list: List<T>) : Iterable<T> {

    val rawValue = serializer.encode(list)
    override operator fun iterator() = list.iterator()
    override fun toString() = "EnumList[${list.joinToString()}]"

    companion object {

        fun <T : SerializableEnum<T>> empty() = EnumList<T>(EmptyEnumSerializer(), emptyList())

    }

}

class EmptyEnumSerializer<T : SerializableEnum<T>> : EnumSerializer<T> {
    override fun decode(value: Long) = EnumList(this, emptyList())

    override fun encode(list: List<T>) = 0L

    override val values
        get() = emptyList<T>()
}