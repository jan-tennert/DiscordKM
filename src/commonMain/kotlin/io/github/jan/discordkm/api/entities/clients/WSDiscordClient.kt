/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.clients

import co.touchlab.stately.collections.IsoMutableMap
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import com.soywiz.klogger.Logger
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.UserCacheEntry
import io.github.jan.discordkm.api.entities.activity.PresenceModifier
import io.github.jan.discordkm.api.entities.containers.CacheChannelContainer
import io.github.jan.discordkm.api.entities.containers.CacheGuildContainer
import io.github.jan.discordkm.api.entities.containers.CacheMemberContainer
import io.github.jan.discordkm.api.entities.containers.CacheThreadContainer
import io.github.jan.discordkm.api.entities.containers.CacheUserContainer
import io.github.jan.discordkm.api.entities.guild.member.MemberCacheEntry
import io.github.jan.discordkm.api.entities.message.Message
import io.github.jan.discordkm.api.entities.misc.TranslationManager
import io.github.jan.discordkm.api.events.Event
import io.github.jan.discordkm.api.events.EventListener
import io.github.jan.discordkm.api.events.ShardCreateEvent
import io.github.jan.discordkm.internal.DiscordKMInternal
import io.github.jan.discordkm.internal.caching.CacheFlag
import io.github.jan.discordkm.internal.caching.ClientCacheManager
import io.github.jan.discordkm.internal.restaction.Requester
import io.github.jan.discordkm.internal.serialization.UpdatePresencePayload
import io.github.jan.discordkm.internal.utils.LoggerConfig
import io.github.jan.discordkm.internal.utils.log
import io.github.jan.discordkm.internal.utils.safeValues
import io.github.jan.discordkm.internal.websocket.Compression
import io.github.jan.discordkm.internal.websocket.DiscordGateway
import io.github.jan.discordkm.internal.websocket.Encoding
import io.ktor.client.HttpClientConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

sealed interface WSDiscordClient : DiscordClient {

    val eventListeners: MutableList<EventListener>
    val shardConnections: Map<Int, DiscordGateway>
    override val channels: CacheChannelContainer
    override val members: CacheMemberContainer
    override val threads: CacheThreadContainer
    override val guilds: CacheGuildContainer
    override val users: CacheUserContainer
    override val selfUser: UserCacheEntry

    suspend fun modifyActivity(modifier: PresenceModifier.() -> Unit)

    suspend fun handleEvent(event: Event) = coroutineScope {
        eventListeners.toList().forEach { launch { it(event) } }
    }

}

/*
 * Websocket Client, normally used for bots. You can receive events, automatically use cached entities
 */
@PublishedApi
internal class WSDiscordClientImpl internal constructor(
    override val config: ClientConfig
) : WSDiscordClient {
    override val shardConnections = mutableMapOf<Int, DiscordGateway>()

    private val mutex = Mutex()
    val lastMessages = IsoMutableMap<Snowflake, Message>()
    val cacheManager = ClientCacheManager(this)
    override val requester = Requester(config)
    override val eventListeners = mutableListOf<EventListener>()
    override val guilds: CacheGuildContainer
        get() = CacheGuildContainer(this, cacheManager.guildCache.safeValues)
    override val channels: CacheChannelContainer
        get() = CacheChannelContainer(cacheManager.guildCache.safeValues.map { it.channels.values }.flatten())
    override val members: CacheMemberContainer
        get() = CacheMemberContainer(cacheManager.guildCache.safeValues.map { it.members.values }.flatten())
    override val users: CacheUserContainer
        get() = CacheUserContainer(this, members.map(MemberCacheEntry::user).map { it.cache!! }.distinctBy { it.id.long })
    override val threads: CacheThreadContainer
        get() = CacheThreadContainer(cacheManager.guildCache.safeValues.map { it.threads.values }.flatten())
    override lateinit var selfUser: UserCacheEntry
    private val LOGGER = config.map<LoggerConfig>("logging")("DiscordKM")

    init {
        if (config.map<Set<Int>>("shards").isEmpty()) shardConnections[0] = DiscordGateway(config, this, 0) else config.map<Set<Int>>("shards").forEach {
            shardConnections[it] = DiscordGateway(config, this, it)
        }
        LOGGER.log(true, Logger.Level.WARN) {
            "Warning: This is a beta version of DiscordKM, please report any bugs you find!"
        }
    }

    override suspend fun modifyActivity(modifier: PresenceModifier.() -> Unit) {
        shardConnections[0]?.send(UpdatePresencePayload(PresenceModifier().apply(modifier)))
    }

    override suspend fun login() {
        shardConnections.forEach { (shard, gateway) ->
            gateway.start(false); if (config.map<Int>("totalShards") != -1) handleEvent(
            ShardCreateEvent(
                this,
                shard
            )
        )
        }
    }

    override suspend fun disconnect() {
        requester.http.close()
        shardConnections.forEach { it.value.close() }
    }

    suspend fun updateSelfUser(user: UserCacheEntry) = mutex.withLock {
        selfUser = user
    }

}


/*
 * Websocket Client, normally used for bots. You can receive events, automatically use cached entities
 */
class DiscordWebSocketClientBuilder @DiscordKMInternal constructor(var token: String) {
    /*
     * The encoding used for the websocket. Currently, only [Encoding.JSON] is supported
     */
    var encoding = Encoding.JSON

    /*
     * The compression used for the websocket. Currently, no compression is supported
     */
    var compression = Compression.NONE

    /*
     * The maximum amount of tries to reconnect (and resume) to the websocket.
     */
    var maxResumeTries = 3

    /*
     * The intents specify which events you should receive. For example if you don't use VoiceStates remove the [Intent.GUILD_VOICE_STATES] intent
     */
    var intents = mutableSetOf<Intent>()

    private var activity = PresenceModifier()

    /*
     * How much the client should wait before reconnecting to a disconnected websocket session
     */
    var reconnectDelay: TimeSpan = 5.seconds

    /*
     * The cache specifies which entities should be cached.
     */
    var enabledCache = CacheFlag.ALL.toMutableSet()

    /*
     * The translation manager can be used to multi-language bots
     */
    var translationManager: TranslationManager = TranslationManager.empty()

    /*
     * Configures the logger
     */
    var logging = LoggerConfig()

    private val shards = mutableSetOf<Int>()

    private var httpClientConfig: HttpClientConfig<*>.() -> Unit = {}
    /*
     * Specifies the total amount of shards. [Sharding](https://discord.com/developers/docs/topics/gateway#sharding)
     */
    var totalShards = -1

    /*
     * Use only specific shards. For more information see [Sharding](https://discord.com/developers/docs/topics/gateway#sharding)
     */
    fun useShards(vararg shards: Int) {
        this.shards.addAll(shards.toList())
    }

    /*
     * Adds all intents except [intents]
     */
    fun intentsWithout(vararg intents: Intent) {
        this.intents.addAll(Intent.values().subtract(intents.toSet()))
    }

    /*
     * Adds [intents] to the discord client
     */
    fun intents(vararg intents: Intent) {
        this.intents.addAll(intents.toList())
    }

    /*
    * Adds [intents] to the discord client
    */
    fun intents(intents: Set<Intent>) {
        this.intents.addAll(intents)
    }

    /*
     * Sets the default activity which is set after connecting to the websocket.
     */
    fun activity(builder: PresenceModifier.() -> Unit) {
        activity = PresenceModifier().apply(builder)
    }

    fun httpClient(builder: HttpClientConfig<*>.() -> Unit) {
        httpClientConfig = builder
    }

    fun build(): WSDiscordClient = WSDiscordClientImpl(
        ClientConfig(mapOf(
            "token" to token,
            "intents" to intents,
            "logging" to logging,
            "enabledCache" to enabledCache,
            "httpClientConfig" to httpClientConfig,
            "totalShards" to totalShards,
            "shards" to shards,
            "reconnectDelay" to reconnectDelay,
            "activity" to activity.activity,
            "status" to activity.status,
            "encoding" to encoding,
            "compression" to compression,
            "maxResumeTries" to maxResumeTries,
            "translationManager" to translationManager
        ))
    )

}


/*
 * Websocket Client, normally used for bots. You can receive events, automatically use cached entities
 */
@OptIn(DiscordKMInternal::class)
inline fun buildClient(token: String, builder: DiscordWebSocketClientBuilder.() -> Unit = {}) =
    DiscordWebSocketClientBuilder(token).apply(builder).build()


inline fun <reified E : Event> WSDiscordClient.on(
    crossinline predicate: (E) -> Boolean = { true },
    crossinline onEvent: suspend E.() -> Unit
) {
    val eventListener = EventListener {
        if (it is E && predicate(it)) {
            onEvent(it)
        }
    }
    eventListeners += eventListener
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend inline fun <reified E : Event> WSDiscordClient.awaitEvent(crossinline predicate: (E) -> Boolean = { true }) =
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

suspend inline fun <reified E : Event> WSDiscordClient.awaitEventWithTimeout(timeout: TimeSpan, crossinline predicate: (E) -> Boolean = { true }) = withTimeoutOrNull(timeout.millisecondsLong) {
    awaitEvent(predicate)
}