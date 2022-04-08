/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.utils

import co.touchlab.stately.collections.IsoMutableMap

operator fun <T> T.plus(other: T) = listOf(this, other)

fun <K, V> Map<K, V>.toIsoMap() = IsoMutableMap { this@toIsoMap.toMutableMap() }

fun <T>Collection<T>.ifNotEmpty(action: () -> Unit) = if(isNotEmpty()) action() else Unit

val <K, V> IsoMutableMap<K, V>.safeValues: List<V> get() {
    val list = mutableListOf<V>()
    values.forEach { list.add(it) }
    return list
}