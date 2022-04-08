/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.interactions.InteractionOption
import kotlin.reflect.KProperty

class OptionContainer(val raw: List<InteractionOption>) : Iterable<InteractionOption> {

    inline operator fun <reified T> get(name: String) : T = getOrNull(name)  ?: throw IllegalArgumentException("Option with name $name does not exist")

    inline operator fun <reified T> get(name: String, default: T) : T = getOrNull(name) ?: default

    inline operator fun <reified T> get(position: Int) = getOrNull<T>(position) ?: throw IllegalArgumentException("Option on position $position does not exist")

    inline operator fun <reified T> get(position: Int, default: T) : T = getOrNull(position) ?: default

    inline fun <reified T> getOrNull(name: String) = raw.firstOrNull { it.name == name }?.let {
        when(T::class) {
            InteractionOption::class -> it as? T
            else -> it.value as? T
        }
    }

    inline fun <reified T> getOrNull(position: Int) = raw.getOrNull(position)?.let {
        when(T::class) {
            InteractionOption::class -> it as? T
            else -> (it.value as? T)
        }
    }

    override fun iterator() = raw.iterator()

    inline operator fun <reified T> getValue(thisRef: Any?, property: KProperty<*>) = get<T>(property.name)

}