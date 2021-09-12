package io.github.jan.discordkm.events

import io.github.jan.discordkm.entities.BaseEntity
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.channels.MessageChannel
import io.github.jan.discordkm.entities.messages.Message
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.toJsonObject

sealed interface MessageEvent : BaseEntity, Event {

    val messageId: Snowflake

    val channelId: Snowflake

    val channel: MessageChannel

    suspend fun retrieveMessage() = client.buildRestAction<Message> {
        action = RestAction.Action.get("/channels/${channelId}/messages/$messageId")
        transform { Message(channel, it.toJsonObject()) }
    }

}