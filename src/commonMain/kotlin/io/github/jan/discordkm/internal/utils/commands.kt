package io.github.jan.discordkm.internal.utils

import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.events.CommandEvent
import io.github.jan.discordkm.api.events.MessageCommandEvent
import io.github.jan.discordkm.api.events.SlashCommandEvent
import io.github.jan.discordkm.api.events.UserCommandEvent

@PublishedApi
internal inline fun <reified E : CommandEvent> DiscordWebSocketClient.onCommand(name: String, crossinline action: suspend E.() -> Unit) = on<E>(predicate = { it.commandName == name }) { action(this) }

inline fun DiscordWebSocketClient.onChatInputCommand(name: String, crossinline action: suspend SlashCommandEvent.() -> Unit) = onCommand(name, action)

inline fun DiscordWebSocketClient.onMessageCommand(name: String, crossinline action: suspend MessageCommandEvent.() -> Unit) = onCommand(name, action)

inline fun DiscordWebSocketClient.onUserCommand(name: String, crossinline action: suspend UserCommandEvent.() -> Unit) = onCommand(name, action)