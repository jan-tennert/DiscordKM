/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.interactions.components.ActionRow
import io.github.jan.discordkm.api.entities.interactions.components.ComponentWithId
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class ComponentContainer<S : ComponentWithId>(val rows: List<ActionRow>) : List<S> by rows.flatMap(ActionRow::components).map({ it as S }) {

    inline operator fun <reified T : S> get(id: String) = (this.firstOrNull { it.customId == id } as? T) ?: throw IllegalArgumentException("No component with id $id and type ${T::class.simpleName} found")

    inline operator fun <reified T : S> getValue(thisRef: Any?, property: KProperty<*>) = get<T>(property.name)

}