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
import io.github.jan.discordkm.api.events.Event
import io.github.jan.discordkm.api.events.EventListener
import io.github.jan.discordkm.api.events.ShardCreateEvent
import io.github.jan.discordkm.internal.caching.CacheFlag
import io.github.jan.discordkm.internal.serialization.UpdatePresencePayload
import io.github.jan.discordkm.internal.websocket.Compression
import io.github.jan.discordkm.internal.websocket.DiscordGateway
import io.github.jan.discordkm.internal.websocket.Encoding
import io.ktor.client.HttpClientConfig
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.jvm.JvmName

/**
 * Websocket Client, normally used for bots. You can receive events, automatically use cached entities
 */
class DiscordWebSocketClient internal constructor(
    token: String,
    private val encoding: Encoding,
    private val compression: Compression,
    val intents: Set<Intent>,
    loggingLevel: Logger.Level,
    private val status: PresenceStatus,
    private val activity: Presence?,
    private val reconnectDelay: TimeSpan = 5.seconds,
    enabledCache: Set<CacheFlag>,
    shards: List<Int> = emptyList(),
    private val totalShards: Int = -1,
    httpClient: HttpClientConfig<*>.() -> Unit,
) : Client(token, loggingLevel, enabledCache, httpClient) {

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
        if(loggedIn) throw IllegalStateException("Discord Client already connected to the discord gateway")
        loggedIn = true
        shardConnections.forEach { it.start(); if(totalShards != -1) handleEvent(ShardCreateEvent(this@DiscordWebSocketClient, it.shardId)) }
    }

    suspend fun disconnect() {
        if(!loggedIn) throw IllegalStateException("Discord Client is already disconnected from the discord gateway")
        loggedIn = false
        shardConnections.forEach { it.close() }
    }

    suspend fun handleEvent(event: Event) = coroutineScope { eventListeners.forEach { launch { it(event) } } }

}

/**
 * Websocket Client, normally used for bots. You can receive events, automatically use cached entities
 */
class DiscordWebSocketClientBuilder @Deprecated("Use the method buildClient", replaceWith = ReplaceWith("buildClient()", "io.github.jan.discordkm.api.entities.clients.buildClient")) constructor(var token: String) {

    /**
     * The encoding used for the websocket. Currently, only [Encoding.JSON] is supported
     */
    var encoding = Encoding.JSON

    /**
     * The compression used for the websocket. Currently, no compression is supported
     */
    var compression = Compression.NONE

    /**
     * The intents specify which events you should receive. For example if you don't use VoiceStates remove the [Intent.GUILD_VOICE_STATES] intent
     */
    var intents = mutableSetOf<Intent>()

    /**
     * The logging level specifies which messages the console should get. [Logger.Level.DEBUG] receives all messages
     */
    var loggingLevel = Logger.Level.DEBUG
    private var activity = PresenceModifier()

    /**
     * How much the client should wait before reconnecting to a disconnected websocket session
     */
    var reconnectDelay: TimeSpan = 5.seconds

    /**
     * The cache specifies which entities should be cached.
     */
    var enabledCache = CacheFlag.ALL.toMutableSet()
    private val shards = mutableListOf<Int>()
    private var httpClientConfig: HttpClientConfig<*>.() -> Unit = {}

    /**
     * Specifies the total amount of shards. [Sharding](https://discord.com/developers/docs/topics/gateway#sharding)
     */
    var totalShards = -1

    /**
     * Use only specific shards. For more information see [Sharding](https://discord.com/developers/docs/topics/gateway#sharding)
     */
    fun useShards(vararg shards: Int) { this.shards.addAll(shards.toList()) }

    /**
     * Sets the default activity which is set after connecting to the websocket.
     */
    fun activity(builder: PresenceModifier.() -> Unit) { activity = PresenceModifier().apply(builder) }

    fun httpClient(builder: HttpClientConfig<*>.() -> Unit) { httpClientConfig = builder }

    fun build() = DiscordWebSocketClient(token, encoding, compression, intents.toSet(), loggingLevel, activity.status, activity.activity, reconnectDelay, enabledCache, shards, totalShards, httpClientConfig)

}

/**
 * Websocket Client, normally used for bots. You can receive events, automatically use cached entities
 */
inline fun buildClient(token: String, builder: DiscordWebSocketClientBuilder.() -> Unit = {}) = DiscordWebSocketClientBuilder(token).apply(builder).build()
