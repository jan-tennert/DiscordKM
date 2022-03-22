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
import io.github.jan.discordkm.api.entities.activity.PresenceModifier
import io.github.jan.discordkm.api.entities.misc.TranslationManager
import io.github.jan.discordkm.api.events.Event
import io.github.jan.discordkm.api.events.EventListener
import io.github.jan.discordkm.api.events.ShardCreateEvent
import io.github.jan.discordkm.internal.DiscordKMInternal
import io.github.jan.discordkm.internal.caching.CacheFlag
import io.github.jan.discordkm.internal.serialization.UpdatePresencePayload
import io.github.jan.discordkm.internal.utils.LoggerConfig
import io.github.jan.discordkm.internal.websocket.Compression
import io.github.jan.discordkm.internal.websocket.DiscordGateway
import io.github.jan.discordkm.internal.websocket.Encoding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Websocket Client, normally used for bots. You can receive events, automatically use cached entities
 */
class DiscordWebSocketClient internal constructor(
    config: ClientConfig
) : Client(config) {

    val shardConnections = mutableListOf<DiscordGateway>()
    val shardById: Map<Int, DiscordGateway> get() = shardConnections.associateBy { it.shardId }

    init {
        if (config.shards.isEmpty()) shardConnections.add(DiscordGateway(config, this, 0)) else config.shards.forEach {
            shardConnections.add(DiscordGateway(config, this, it))
        }
    }

    inline fun <reified E : Event> on(
        crossinline predicate: (E) -> Boolean = { true },
        crossinline onEvent: suspend E.() -> Unit
    ) {
        eventListeners += EventListener {
            if (it is E && predicate(it)) {
                onEvent(it)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend inline fun <reified E : Event> awaitEvent(crossinline predicate: (E) -> Boolean = { true }) =
        suspendCancellableCoroutine<E> {
            val listener = object : EventListener {
                override suspend fun onEvent(event: Event) {
                    if (event is E && predicate(event)) {
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

    override suspend fun login() {
        shardConnections.forEach {
            it.start(false); if (config.totalShards != -1) handleEvent(
            ShardCreateEvent(
                this@DiscordWebSocketClient,
                it.shardId
            )
        )
        }
    }

    override suspend fun disconnect() {
       // requester.http.
        shardConnections.forEach { it.close() }
    }

}

/**
 * Websocket Client, normally used for bots. You can receive events, automatically use cached entities
 */
class DiscordWebSocketClientBuilder @DiscordKMInternal constructor(var token: String) {

    /**
     * The encoding used for the websocket. Currently, only [Encoding.JSON] is supported
     */
    var encoding = Encoding.JSON

    /**
     * The compression used for the websocket. Currently, no compression is supported
     */
    var compression = Compression.NONE

    /**
     * The maximum amount of tries to reconnect (and resume) to the websocket.
     */
    var maxResumeTries = 3

    /**
     * The intents specify which events you should receive. For example if you don't use VoiceStates remove the [Intent.GUILD_VOICE_STATES] intent
     */
    var intents = mutableSetOf<Intent>()

    private var activity = PresenceModifier()

    /**
     * How much the client should wait before reconnecting to a disconnected websocket session
     */
    var reconnectDelay: TimeSpan = 5.seconds

    /**
     * The cache specifies which entities should be cached.
     */
    var enabledCache = CacheFlag.ALL.toMutableSet()

    /**
     * The translation manager can be used to multi-language bots
     */
    var translationManager: TranslationManager = TranslationManager.empty()

    /**
     * Configures the logger
     */
    var logging = LoggerConfig()

    private val shards = mutableSetOf<Int>()

    /**
     * Specifies the total amount of shards. [Sharding](https://discord.com/developers/docs/topics/gateway#sharding)
     */
    var totalShards = -1

    /**
     * Use only specific shards. For more information see [Sharding](https://discord.com/developers/docs/topics/gateway#sharding)
     */
    fun useShards(vararg shards: Int) {
        this.shards.addAll(shards.toList())
    }

    fun intents(vararg intents: Intent) {
        this.intents.addAll(intents.toList())
    }

    fun intents(intents: Set<Intent>) {
        this.intents.addAll(intents)
    }

    /**
     * Sets the default activity which is set after connecting to the websocket.
     */
    fun activity(builder: PresenceModifier.() -> Unit) {
        activity = PresenceModifier().apply(builder)
    }

    fun build() = DiscordWebSocketClient(
        ClientConfig(
            token,
            intents,
            logging,
            enabledCache,
            totalShards,
            shards,
            reconnectDelay,
            activity.activity,
            activity.status,
            encoding,
            compression,
            maxResumeTries,
            translationManager
        )
    )

}


/**
 * Websocket Client, normally used for bots. You can receive events, automatically use cached entities
 */
@OptIn(DiscordKMInternal::class)
inline fun buildClient(token: String, builder: DiscordWebSocketClientBuilder.() -> Unit = {}) =
    DiscordWebSocketClientBuilder(token).apply(builder).build()
