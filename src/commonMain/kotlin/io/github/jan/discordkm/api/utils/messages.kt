/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.utils

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.clients.WSDiscordClient
import io.github.jan.discordkm.api.entities.clients.awaitEvent
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.api.events.MessageCreateEvent
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull

suspend fun MessageChannel.awaitMessages(
    amount: Int = 1,
    timeout: TimeSpan? = null,
) : List<Message> {
    val messages = mutableListOf<Message>()
    if(timeout != null) {
        withTimeoutOrNull(timeout.millisecondsLong) {
            while(messages.size != amount && isActive) {
                messages.add((client as WSDiscordClient).awaitEvent<MessageCreateEvent> { it.channel.id == id }.message)
            }
        }
    } else {
        while(messages.size != amount) {
            messages.add((client as WSDiscordClient).awaitEvent<MessageCreateEvent> { it.channel.id == id }.message)
        }
    }
    return messages
}