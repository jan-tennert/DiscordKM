/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.clients

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import com.soywiz.klogger.Logger
import io.github.jan.discordkm.api.entities.activity.ActivityModifier
import io.github.jan.discordkm.api.entities.activity.DiscordActivity
import io.github.jan.discordkm.api.entities.activity.PresenceStatus
import io.github.jan.discordkm.api.entities.misc.EnumList
import io.github.jan.discordkm.api.events.Event
import io.github.jan.discordkm.api.events.EventListener
import io.github.jan.discordkm.internal.serialization.UpdatePresencePayload
import io.github.jan.discordkm.internal.websocket.Compression
import io.github.jan.discordkm.internal.websocket.DiscordGateway
import io.github.jan.discordkm.internal.websocket.Encoding
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.jvm.JvmName

class DiscordClient internal constructor(
    token: String,
    encoding: Encoding,
    compression: Compression,
    val intents: EnumList<Intent>,
    loggingLevel: Logger.Level,
    status: PresenceStatus,
    activity: DiscordActivity?,
    reconnectDelay: TimeSpan = 5.seconds
) : Client(token, loggingLevel) {

    internal val gateway = DiscordGateway(encoding, compression, this, status, activity, reconnectDelay)
    @get:JvmName("isClosed")
    var isDisconnected = false
        private set
    var loggedIn = false
        private set
    val eventListeners = mutableListOf<EventListener>()

    inline fun <reified E : Event> on(crossinline predicate: (E) -> Boolean = { true }, crossinline onEvent: suspend E.() -> Unit) {
        eventListeners += EventListener {
            if(it is E && predicate(it)) {
                onEvent(it)
            }
        }
    }

    suspend fun modifyActivity(modifier: ActivityModifier.() -> Unit) = gateway.send(UpdatePresencePayload(ActivityModifier().apply(modifier)))

    suspend fun login() {
        if(loggedIn) throw UnsupportedOperationException("Discord Client already connected to the discord gateway")
        gateway.start()
        loggedIn = true
    }

    fun disconnect() {
        if(isDisconnected) throw UnsupportedOperationException("Discord Client is already disconnected from the discord gateway")
        gateway.close()
        isDisconnected = true
    }

    internal suspend fun handleEvent(event: Event) = coroutineScope { eventListeners.forEach { launch { it(event) } } }

}

class DiscordClientBuilder @Deprecated("Use the method buildClient", replaceWith = ReplaceWith("buildClient()", "io.github.jan.discordkm.api.entities.clients.buildClient")) constructor(var token: String) {

    var encoding = Encoding.JSON
    var compression = Compression.NONE
    var intents = mutableListOf<Intent>()
    var loggingLevel = Logger.Level.DEBUG
    private var activity = ActivityModifier()
    var reconnectDelay: TimeSpan = 5.seconds

    fun activity(builder: ActivityModifier.() -> Unit) { activity = ActivityModifier().apply(builder) }

    fun build() = DiscordClient(token, encoding, compression, EnumList(Intent, intents), loggingLevel, activity.status, activity.activity, reconnectDelay)

}

inline fun buildClient(token: String, builder: DiscordClientBuilder.() -> Unit = {}) = DiscordClientBuilder(token).apply(builder).build()
