/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.events.MessageDeleteEvent
import io.github.jan.discordkm.internal.caching.Cache
import io.github.jan.discordkm.internal.entities.channels.MessageChannel
import io.github.jan.discordkm.internal.entities.channels.MessageChannelData
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject

class MessageDeleteEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<MessageDeleteEvent> {

    override fun handle(data: JsonObject): MessageDeleteEvent {
        val channelId = data.getOrThrow<Snowflake>("channel_id")
        val channel = (client.channels[channelId] ?: client.threads[channelId] ?: MessageChannelData.fromId(client, channelId)) as MessageChannel
        val messageId = data.getOrThrow<Snowflake>("id")
        if(Cache.MESSAGES in client.enabledCache) channel.messageCache.remove(messageId)
        return MessageDeleteEvent(client, messageId, channel)
    }

}