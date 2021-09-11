package com.github.jan.discordkm.entities.channels

import com.github.jan.discordkm.entities.messages.DataMessage
import com.github.jan.discordkm.entities.messages.EmbedBuilder
import com.github.jan.discordkm.entities.messages.Message
import com.github.jan.discordkm.entities.messages.buildEmbed
import com.github.jan.discordkm.entities.messages.buildMessage
import com.github.jan.discordkm.restaction.RestAction
import com.github.jan.discordkm.restaction.buildRestAction
import com.github.jan.discordkm.utils.extractClientEntity
import com.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface MessageChannel : Channel {

    suspend fun sendMessage(message: DataMessage) = client.buildRestAction<Message> {
        action = RestAction.Action.post("/channels/$id/messages", Json.encodeToString(message))
        transform {
            it.toJsonObject().extractClientEntity(client)
        }
    }

    suspend fun sendMessage(content: String) = sendMessage(buildMessage { this.content = content })

    suspend fun sendEmbed(embed: EmbedBuilder.() -> Unit) = sendMessage(buildMessage { embeds += buildEmbed(embed) })

   // fun sendFile(file: VfsFile) : RestAction<DataMessage>

}