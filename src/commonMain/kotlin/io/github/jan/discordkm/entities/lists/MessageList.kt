package io.github.jan.discordkm.entities.lists

import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.channels.MessageChannel
import io.github.jan.discordkm.entities.channels.PrivateChannel
import io.github.jan.discordkm.entities.messages.Message
import io.github.jan.discordkm.restaction.CallsTheAPI
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildQuery
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractMessageChannelEntity
import io.github.jan.discordkm.utils.toJsonArray
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.json.jsonObject

class MessageList(val channel: MessageChannel, override val internalList: List<Message>) : DiscordList<Message> {

    override fun get(content: String) = internalList.filter { it.content == content }

    /**
     * Retrieves a message from its id
     */
    @CallsTheAPI
    suspend fun retrieve(id: Snowflake) = channel.client.buildRestAction<Message> {
        action = RestAction.Action.get("/channels/${channel.id}/messages/$id")
        transform { it.toJsonObject().extractMessageChannelEntity(channel) }
        onFinish { channel.messageCache[it.id] = it }
    }

    /**
     * Retrieves messages from the channel
     * @param limit How much you want to retrieve
     *
     * **You can only use one snowflake parameter**
     */
    @CallsTheAPI
    suspend fun retrieveMessage(limit: Int? = null, before: Snowflake? = null, after: Snowflake? = null, around: Snowflake? = null) = channel.client.buildRestAction<List<Message>> {
        action = RestAction.Action.get("/channels/${channel.id}/messages" + buildQuery {
            putOptional("limit", limit)
            if(before != null) put("before", before) else if(after != null) put("after", after) else if(around != null) put("around", around)
        })
        transform { it.toJsonArray().map { json -> Message(channel, json.jsonObject) } }
    }

    /**
     * Retrieves all pinned messages in this channel
     */
    @CallsTheAPI
    suspend fun retrievePinnedMessages() = if(channel is PrivateChannel) emptyList() else channel.client.buildRestAction<List<Message>> {
        action = RestAction.Action.get("/channels/$${channel.id}/pins")
        transform {
            it.toJsonArray().map { Message(channel, it.jsonObject) }
        }
    }

}