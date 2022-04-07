/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.api.events.MessageCreateEvent
import io.github.jan.discordkm.internal.utils.snowflake
import kotlinx.serialization.json.JsonObject

internal class MessageCreateEventHandler(val client: Client) : InternalEventHandler<MessageCreateEvent> {

    override suspend fun handle(data: JsonObject): MessageCreateEvent {
        val channel = MessageChannel(data["channel_id"]!!.snowflake, client)
        val message = Message(data, client)
        (client as DiscordWebSocketClient).lastMessages[channel.id] = message
        channel.cache?.cacheManager?.messageCache?.set(message.id, message)
        return MessageCreateEvent(client, message, channel)
    }

}