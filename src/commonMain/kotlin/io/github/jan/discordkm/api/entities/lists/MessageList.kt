package io.github.jan.discordkm.api.entities.lists

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.entities.channels.MessageChannel
import io.github.jan.discordkm.internal.entities.channels.MessageChannelData
import io.github.jan.discordkm.internal.entities.channels.PrivateChannel
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.restaction.RestAction
import io.github.jan.discordkm.internal.restaction.buildQuery
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractMessageChannelEntity
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.jsonObject

class MessageList(val channel: MessageChannel, override val internalMap: Map<Snowflake, Message>) : SnowflakeList<Message> {

    /**
     * Retrieves a message from its id
     */

    suspend fun retrieve(id: Snowflake) = channel.client.buildRestAction<Message> {
        route = Route.Message.GET_MESSAGE(channel.id, id).get()
        transform { it.toJsonObject().extractMessageChannelEntity(channel) }
        onFinish { (channel as MessageChannelData).messageCache[it.id] = it }
    }

    /**
     * Retrieves messages from the channel
     * @param limit How much you want to retrieve
     *
     * **You can only use one snowflake parameter**
     */

    suspend fun retrieveMessage(limit: Int? = null, before: Snowflake? = null, after: Snowflake? = null, around: Snowflake? = null) = channel.client.buildRestAction<List<Message>> {
        route = RestAction.get("/channels/${channel.id}/messages" + buildQuery {
            putOptional("limit", limit)
            if(before != null) put("before", before) else if(after != null) put("after", after) else if(around != null) put("around", around)
        })
        transform { it.toJsonArray().map { json -> Message(channel, json.jsonObject) } }
    }

    /**
     * Retrieves all pinned messages in this channel
     */

    suspend fun retrievePinnedMessages() = if(channel is PrivateChannel) emptyList() else channel.client.buildRestAction<List<Message>> {
        route = RestAction.get("/channels/$${channel.id}/pins")
        transform {
            it.toJsonArray().map { Message(channel, it.jsonObject) }
        }
    }

}