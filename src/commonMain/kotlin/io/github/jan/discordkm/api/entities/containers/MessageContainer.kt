/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.channels.PrivateChannel
import io.github.jan.discordkm.api.entities.message.Message
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.jsonObject

class MessageContainer(override val values: Collection<Message>, val channel: MessageChannel) : SnowflakeContainer<Message> {

    /*
     * Retrieves a message from its id
     */
    suspend fun retrieve(id: Snowflake) = channel.client.buildRestAction<Message> {
        route = Route.Message.GET_MESSAGE(channel.id, id).get()
        transform { Message(it.toJsonObject(), channel.client) }
    }

    /*
     * Retrieves messages from the channel
     * @param limit How much you want to retrieve
     *
     * **You can only use one snowflake parameter**
     */
    suspend fun retrieveMessage(limit: Int? = null, before: Snowflake? = null, after: Snowflake? = null, around: Snowflake? = null) = channel.client.buildRestAction<List<Message>> {
        route = Route.Message.GET_MESSAGES(channel.id.string).get {
            putOptional("limit", limit)
            if(before != null) put("before", before) else if(after != null) put("after", after) else if(around != null) put("around", around)
        }
        transform { it.toJsonArray().map { json -> Message(json.jsonObject, channel.client) } }
    }

    /*
     * Retrieves all pinned messages in this channel
     */
    suspend fun retrievePinnedMessages() = if(channel is PrivateChannel) emptyList() else channel.client.buildRestAction<List<Message>> {
        route = Route.Message.GET_PINNED_MESSAGES(channel.id.string).get()
        transform {
            it.toJsonArray().map { msg -> Message(msg.jsonObject, channel.client) }
        }
    }

}