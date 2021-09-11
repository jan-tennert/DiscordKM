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