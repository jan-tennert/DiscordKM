/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.utils

import io.github.jan.discordkm.api.entities.clients.WSDiscordClient
import io.github.jan.discordkm.api.entities.clients.on
import io.github.jan.discordkm.api.events.CommandEvent
import io.github.jan.discordkm.api.events.MessageCommandEvent
import io.github.jan.discordkm.api.events.SlashCommandEvent
import io.github.jan.discordkm.api.events.UserCommandEvent

@PublishedApi
internal inline fun <reified E : CommandEvent> WSDiscordClient.onCommand(name: String, crossinline action: suspend E.() -> Unit) = on<E>(predicate = { it.commandName == name }) { action(this) }

inline fun WSDiscordClient.onChatInputCommand(name: String, crossinline action: suspend SlashCommandEvent.() -> Unit) = onCommand(name, action)

inline fun WSDiscordClient.onMessageCommand(name: String, crossinline action: suspend MessageCommandEvent.() -> Unit) = onCommand(name, action)

inline fun WSDiscordClient.onUserCommand(name: String, crossinline action: suspend UserCommandEvent.() -> Unit) = onCommand(name, action)