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
import io.github.jan.discordkm.api.entities.activity.Presence
import io.github.jan.discordkm.api.entities.activity.PresenceModifier
import io.github.jan.discordkm.api.entities.activity.PresenceStatus
import io.github.jan.discordkm.api.entities.misc.EnumList
import io.github.jan.discordkm.api.events.Event
import io.github.jan.discordkm.api.events.EventListener
import io.github.jan.discordkm.api.events.ShardCreateEvent
import io.github.jan.discordkm.internal.Cache
import io.github.jan.discordkm.internal.serialization.UpdatePresencePayload
import io.github.jan.discordkm.internal.websocket.Compression
import io.github.jan.discordkm.internal.websocket.DiscordGateway
import io.github.jan.discordkm.internal.websocket.Encoding
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.jvm.JvmName

class DiscordWebSocketClient internal constructor(
    token: String,
    private val encoding: Encoding,
    private val compression: Compression,
    val intents: EnumList<Intent>,
    loggingLevel: Logger.Level,
    private val status: PresenceStatus,
    private val activity: Presence?,
    private val reconnectDelay: TimeSpan = 5.seconds,
    internal val enabledCache: List<Cache>,
    private val shards: List<Int> = emptyList(),
    private val totalShards: Int = -1
) : Client(token, loggingLevel) {

    val shardConnections = mutableListOf<DiscordGateway>()
    @get:JvmName("isClosed")
    var loggedIn = false
        private set
    val eventListeners = mutableListOf<EventListener>()

    init {
        if(shards.isEmpty()) shardConnections.add(DiscordGateway(encoding, compression, this, status, activity, reconnectDelay, 0, -1)) else shards.forEach {
            shardConnections.add(DiscordGateway(encoding, compression, this, status, activity, reconnectDelay, it, totalShards))
        }
    }

    internal fun getGatewayByShardId(shardId: Int) = shardConnections.first { it.shardId == shardId }

    inline fun <reified E : Event> on(crossinline predicate: (E) -> Boolean = { true }, crossinline onEvent: suspend E.() -> Unit) {
        eventListeners += EventListener {
            if(it is E && predicate(it)) {
                onEvent(it)
            }
        }
    }

    suspend inline fun <reified E : Event> awaitEvent(crossinline predicate: (E) -> Boolean = { true }) = suspendCancellableCoroutine<E> {
        val listener = object : EventListener {
            override suspend fun onEvent(event: Event) {
                if(event is E && predicate(event)) {
                    it.resume(event) { err -> throw err }
                    eventListeners -= this
                }
            }
        }
        eventListeners += listener
        it.invokeOnCancellation {
            eventListeners -= listener
        }
    }

    suspend fun modifyActivity(modifier: PresenceModifier.() -> Unit) = shardConnections[0].send(UpdatePresencePayload(PresenceModifier().apply(modifier)))

    suspend fun login() {
        if(loggedIn) throw UnsupportedOperationException("Discord Client already connected to the discord gateway")
        shardConnections.forEach { it.start(); if(totalShards != -1) handleEvent(ShardCreateEvent(this@DiscordWebSocketClient, it.shardId)) }
        loggedIn = true
    }

    fun disconnect() {
        if(!loggedIn) throw UnsupportedOperationException("Discord Client is already disconnected from the discord gateway")
        shardConnections.forEach { it.close() }
        loggedIn = false
    }

    suspend fun handleEvent(event: Event) = coroutineScope { eventListeners.forEach { launch { it(event) } } }

}

class DiscordWebSocketClientBuilder @Deprecated("Use the method buildClient", replaceWith = ReplaceWith("buildClient()", "io.github.jan.discordkm.api.entities.clients.buildClient")) constructor(var token: String) {

    var encoding = Encoding.JSON
    var compression = Compression.NONE
    var intents = mutableListOf<Intent>()
    var loggingLevel = Logger.Level.DEBUG
    private var activity = PresenceModifier()
    var reconnectDelay: TimeSpan = 5.seconds
    var enabledCache = Cache.STANDARD.toMutableList()
    private val shards = mutableListOf<Int>()
    var totalShards = -1

    fun useShards(vararg shards: Int) { this.shards.addAll(shards.toList()) }

    fun activity(builder: PresenceModifier.() -> Unit) { activity = PresenceModifier().apply(builder) }

    fun build() = DiscordWebSocketClient(token, encoding, compression, EnumList(Intent, intents), loggingLevel, activity.status, activity.activity, reconnectDelay, enabledCache, shards, totalShards)

}

inline fun buildClient(token: String, builder: DiscordWebSocketClientBuilder.() -> Unit = {}) = DiscordWebSocketClientBuilder(token).apply(builder).build()
