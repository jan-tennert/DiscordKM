/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities

import io.github.jan.discordkm.api.entities.misc.EnumList

interface EnumSerializer <T : SerializableEnum<T>> {

    val values: List<T>

    fun decode(value: Long) : EnumList<T> {
        if (value == 0L) return EnumList.empty()
        val list = values.filter { (value and it.rawValue) == it.rawValue; }
        return EnumList(this, list)
    }
    fun encode(list: List<T>) : Long {
        var raw: Long = 0
        list.forEach { raw = raw or it.rawValue }
        return raw
    }

}

interface SerializableEnum <T> {

    val offset: Int
    val rawValue: Long
        get() = 1L shl offset

}