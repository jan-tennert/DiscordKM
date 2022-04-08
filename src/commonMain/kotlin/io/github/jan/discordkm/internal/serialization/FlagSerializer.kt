/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.serialization

open class FlagSerializer<T : FlagEnum<T>>(val values: Collection<T>) {

    constructor(values: Array<T>) : this(values.toList())

    fun decode(value: Long) : Set<T> {
        if (value == 0L) return setOf()
        return values.filter { (value and it.rawValue) == it.rawValue }.toSet()
    }

    fun encode(list: Set<T>) : Long {
        var raw: Long = 0
        list.forEach { raw = raw or it.rawValue }
        return raw
    }

}

inline fun <reified S, reified E>S.encode(serializer: S, values: Set<E>) : Long where S : FlagSerializer<E>, E : Enum<E>, E : FlagEnum<E> {
    return serializer.encode(values)
}

inline fun <reified S, reified E>S.decode(serializer: S, raw: Long) : Set<E> where S : FlagSerializer<E>, E : Enum<E>, E : FlagEnum<E> {
    return serializer.decode(raw)
}

fun <E>Set<E>.rawValue() : Long where E : Enum<E>, E : FlagEnum<E> = FlagSerializer(this).encode(this)