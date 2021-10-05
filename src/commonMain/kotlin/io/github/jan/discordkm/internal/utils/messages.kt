package io.github.jan.discordkm.internal.utils

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.api.events.MessageCreateEvent
import io.github.jan.discordkm.internal.entities.channels.MessageChannel
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
                messages.add((client as DiscordWebSocketClient).awaitEvent<MessageCreateEvent> { it.channelId == id }.message)
            }
        }
    } else {
        while(messages.size != amount) {
            messages.add((client as DiscordWebSocketClient).awaitEvent<MessageCreateEvent> { it.channelId == id }.message)
        }
    }
    return messages
}