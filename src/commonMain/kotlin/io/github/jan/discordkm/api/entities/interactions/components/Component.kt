/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.interactions.components

import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import kotlinx.serialization.Serializable

interface Component {

    /*
     * The type of the component
     */
    val type: ComponentType

}

interface ComponentWithId : Component {

    val customId: String

}

@Serializable(with = ComponentType.Companion::class)
enum class ComponentType : EnumWithValue<Int> {
    ACTION_ROW,
    BUTTON,
    SELECTION_MENU,
    TEXT_INPUT;

    override val value: Int
        get() = ordinal + 1

    companion object : EnumWithValueGetter<ComponentType, Int>(values())

}