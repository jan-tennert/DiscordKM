/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.channels

import io.github.jan.discordkm.entities.messages.DataMessage
import io.github.jan.discordkm.entities.messages.EmbedBuilder
import io.github.jan.discordkm.entities.messages.Message
import io.github.jan.discordkm.entities.messages.buildEmbed
import io.github.jan.discordkm.entities.messages.buildMessage
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractMessageChannelEntity
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface MessageChannel : Channel {

    suspend fun sendMessage(message: DataMessage) = client.buildRestAction<Message> {
        action = RestAction.Action.post("/channels/$id/messages", Json.encodeToString(message))
        transform {
            it.toJsonObject().extractMessageChannelEntity(this@MessageChannel)
        }
    }

    suspend fun sendMessage(content: String) = sendMessage(buildMessage { this.content = content })

    suspend fun sendEmbed(embed: EmbedBuilder.() -> Unit) = sendMessage(buildMessage { embeds += buildEmbed(embed) })

   // fun sendFile(file: VfsFile) : RestAction<DataMessage>

}