/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.interactions.components

import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.interactions.ComponentDsl
import io.github.jan.discordkm.api.entities.interactions.RowLayout
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Suppress("unused")
class RowBuilder<T: RowLayout>(val client: DiscordClient? = null, val components: MutableList<Component> = mutableListOf()) {

    fun build() = ActionRow(components)

}

@Serializable
open class RowLayoutBuilder<T : RowLayout>(@Transient internal open val client: DiscordClient? = null, @SerialName("components") val rows: MutableList<ActionRow> = mutableListOf()) {

    @ComponentDsl
    fun row(builder: RowBuilder<T>.() -> Unit) { rows += RowBuilder<T>(client).apply(builder).build() }

    fun add(row: ActionRow) { rows += row }

}