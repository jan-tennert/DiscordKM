/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.interactions.modals

import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.clients.WSDiscordClient
import io.github.jan.discordkm.api.entities.clients.on
import io.github.jan.discordkm.api.entities.interactions.ComponentDsl
import io.github.jan.discordkm.api.entities.interactions.components.RowLayoutBuilder
import io.github.jan.discordkm.api.events.ModalSubmitEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ModalBuilder(@Transient override val client: DiscordClient? = null, @SerialName("custom_id") var customId: String = "", var title: String = "") : RowLayoutBuilder<ModalLayout>(client) {

    @ComponentDsl
    fun onSubmit(callback: suspend ModalSubmitEvent.() -> Unit) {
        if(client is WSDiscordClient) {
            client.on(predicate = { (it.modalId == customId) }, callback)
        }
    }

}