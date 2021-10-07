/**
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

inline fun <reified T : Enum<T>> valueOfIndex(index: Int, add: Int = 0) = enumValues<T>().first { it.ordinal + add == index }

inline fun <reified T : Enum<T>> valueOfIndexOrDefault(index: Int?, add: Int = 0, default: T) = enumValues<T>().firstOrNull { it.ordinal + add == index } ?: default

inline fun <reified T : Enum<T>> valueOfIndexOrNull(index: Int?, add: Int = 0) = enumValues<T>().firstOrNull { it.ordinal + add == index }

fun <K, V> Map<K, V>.toIsoMap() = IsoMutableMap { this@toIsoMap.toMutableMap() }

inline fun <T> checkAndReturn(check: () -> T) = check()