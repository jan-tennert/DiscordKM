/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.websocket

import co.touchlab.stately.collections.IsoMutableList
import com.soywiz.klock.milliseconds
import com.soywiz.klogger.Logger
import io.github.jan.discordkm.api.entities.clients.ClientConfig
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.events.GuildBanAddEvent
import io.github.jan.discordkm.api.events.GuildBanRemoveEvent
import io.github.jan.discordkm.api.events.RawEvent
import io.github.jan.discordkm.api.events.VoiceServerUpdate
import io.github.jan.discordkm.internal.events.BanEventHandler
import io.github.jan.discordkm.internal.events.ChannelCreateEventHandler
import io.github.jan.discordkm.internal.events.ChannelDeleteEventHandler
import io.github.jan.discordkm.internal.events.ChannelPinUpdateEventHandler
import io.github.jan.discordkm.internal.events.ChannelUpdateEventHandler
import io.github.jan.discordkm.internal.events.GuildCreateEventHandler
import io.github.jan.discordkm.internal.events.GuildDeleteEventHandler
import io.github.jan.discordkm.internal.events.GuildEmojisUpdateEventHandler
import io.github.jan.discordkm.internal.events.GuildMemberAddEventHandler
import io.github.jan.discordkm.internal.events.GuildMemberRemoveEventHandler
import io.github.jan.discordkm.internal.events.GuildMemberUpdateEventHandler
import io.github.jan.discordkm.internal.events.GuildStickersUpdateEventHandler
import io.github.jan.discordkm.internal.events.GuildUpdateEventHandler
import io.github.jan.discordkm.internal.events.InteractionCreateEventHandler
import io.github.jan.discordkm.internal.events.InviteCreateEventHandler
import io.github.jan.discordkm.internal.events.InviteDeleteEventHandler
import io.github.jan.discordkm.internal.events.MessageBulkDeleteEventHandler
import io.github.jan.discordkm.internal.events.MessageCreateEventHandler
import io.github.jan.discordkm.internal.events.MessageDeleteEventHandler
import io.github.jan.discordkm.internal.events.MessageReactionAddEventHandler
import io.github.jan.discordkm.internal.events.MessageReactionEmojiRemoveEventHandler
import io.github.jan.discordkm.internal.events.MessageReactionRemoveAllEventHandler
import io.github.jan.discordkm.internal.events.MessageReactionRemoveEventHandler
import io.github.jan.discordkm.internal.events.MessageUpdateEventHandler
import io.github.jan.discordkm.internal.events.PresenceUpdateEventHandler
import io.github.jan.discordkm.internal.events.ReadyEventHandler
import io.github.jan.discordkm.internal.events.RoleCreateEventHandler
import io.github.jan.discordkm.internal.events.RoleDeleteEventHandler
import io.github.jan.discordkm.internal.events.RoleUpdateEventHandler
import io.github.jan.discordkm.internal.events.ScheduledEventCreateHandler
import io.github.jan.discordkm.internal.events.ScheduledEventDeleteHandler
import io.github.jan.discordkm.internal.events.ScheduledEventUpdateHandler
import io.github.jan.discordkm.internal.events.ScheduledEventUserAddEventHandler
import io.github.jan.discordkm.internal.events.ScheduledEventUserRemoveEventHandler
import io.github.jan.discordkm.internal.events.SelfUserUpdateEventHandler
import io.github.jan.discordkm.internal.events.StageInstanceCreateEventHandler
import io.github.jan.discordkm.internal.events.StageInstanceDeleteEventHandler
import io.github.jan.discordkm.internal.events.StageInstanceUpdateEventHandler
import io.github.jan.discordkm.internal.events.ThreadCreateEventHandler
import io.github.jan.discordkm.internal.events.ThreadDeleteEventHandler
import io.github.jan.discordkm.internal.events.ThreadMembersUpdateEventHandler
import io.github.jan.discordkm.internal.events.ThreadUpdateEventHandler
import io.github.jan.discordkm.internal.events.TypingStartEventHandler
import io.github.jan.discordkm.internal.events.VoiceStateUpdateEventHandler
import io.github.jan.discordkm.internal.events.WebhooksUpdateEventHandler
import io.github.jan.discordkm.internal.serialization.IdentifyPayload
import io.github.jan.discordkm.internal.serialization.Payload
import io.github.jan.discordkm.internal.serialization.rawValue
import io.github.jan.discordkm.internal.serialization.send
import io.github.jan.discordkm.internal.utils.LoggerOutput
import io.github.jan.discordkm.internal.utils.generateWebsocketURL
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.readBytes
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put

//TODO: Ratelimit handling

class DiscordGateway(
    private val config: ClientConfig,
    val client: DiscordWebSocketClient,
    val shardId: Int = 0,
) {

    private val LOGGER = Logger("Websocket")
    private var heartbeatInterval = 0L
    private var lastSequenceNumber: Int? = null
    var sessionId: String? = null
        internal set
    private val http = HttpClient() {
        install(WebSockets)
    }
    private val tasks = IsoMutableList<Task>()
    val mutex = Mutex()
    var isConnected = false
        private set

    init {
        LOGGER.level = config.loggingLevel
        LOGGER.output = LoggerOutput
    }

    suspend fun start(resume: Boolean, delay: Boolean = false) {
        if (delay) com.soywiz.korio.async.delay(config.reconnectDelay)
        LOGGER.info { "Connecting to gateway..." }
        mutex.withLock { isConnected = true }
        http.webSocket(generateWebsocketURL(config.encoding, config.compression)) {
            LOGGER.info { "Connected to gateway!" }
            launch { startRequester() }
            while (isConnected) {
                try {
                    val message = incoming.receive().readBytes().decodeToString()
                    onMessage(message)
                } catch (e: Exception) {
                    LOGGER.error { "Disconnected due to an error: ${closeReason.await()}. Trying to reconnect in ${config.reconnectDelay.seconds} seconds" }
                    mutex.withLock { isConnected = false }
                    launch { start(resume = true, delay = true) }
                }
                com.soywiz.korio.async.delay(1.milliseconds)
            }
        }
        mutex.withLock { isConnected = false }
    }

    private suspend fun DefaultClientWebSocketSession.startRequester() {
        while (isConnected) {
            val tasksCopy = tasks.access { it.toList() }
            for (task in tasksCopy) {
                send(task)
                tasks -= task
            }
            com.soywiz.korio.async.delay(1.milliseconds)
        }
    }

    fun send(payload: Payload) {
        tasks += payload
    }

    private fun onMessage(message: String) {
        val json = Json.parseToJsonElement(message).jsonObject
        var data = json["d"]
        if (data is JsonNull) data = null
        if (data.toString() == "false") data = null
        client.launch {
            onEvent(
                Payload(
                    json.getValue("op").jsonPrimitive.int,
                    data?.jsonObject,
                    json["s"]?.jsonPrimitive?.intOrNull,
                    json["t"]?.jsonPrimitive?.content
                )
            )
        }
    }

    private suspend fun onEvent(payload: Payload) {
        when (OpCode.fromCode(payload.opCode)) {
            OpCode.DISPATCH -> {
                payload.eventName?.let { LOGGER.debug { "Received event $it on shard $shardId" } }
                lastSequenceNumber = payload.sequenceNumber
                handleRawEvent(payload)
            }
            OpCode.HEARTBEAT -> {
                sendHeartbeat()
            }
            OpCode.RECONNECT -> {
                close()
                start(false)
            }
            OpCode.INVALID_SESSION -> {
                LOGGER.warn { "Failed to resume! Trying to reconnect manually..." }
                close()
                this@DiscordGateway.start(true)
            }
            OpCode.HELLO -> {
                heartbeatInterval = payload.eventData!!.jsonObject["heartbeat_interval"]!!.jsonPrimitive.long
                coroutineScope {
                    launch {
                        LOGGER.debug { "Start heartbeating..." }
                        startHeartbeating()
                    }
                    launch {
                        if (sessionId != null) {
                            LOGGER.info { "Trying to resume..." }
                            send(Payload(6, buildJsonObject {
                                put("token", config.token)
                                put("session_id", sessionId)
                                put("seq", lastSequenceNumber)
                            }))
                        } else {
                            LOGGER.debug { "Authenticate..." }
                            send(
                                IdentifyPayload(
                                    config.token,
                                    config.intents.rawValue(),
                                    config.status,
                                    config.activity,
                                    shardId,
                                    config.totalShards
                                )
                            )
                        }
                    }
                }
            }
            OpCode.HEARTBEAT_ACK -> {
                LOGGER.debug { "Received heartbeat acknowledge" }
            }
        }
    }

    private suspend fun startHeartbeating() {
        while (isConnected) {
            delay(heartbeatInterval)
            sendHeartbeat()
            LOGGER.debug { "Sending heartbeat..." }
        }
    }

    private suspend fun sendHeartbeat() = send(Payload(1, JsonPrimitive(heartbeatInterval), lastSequenceNumber, null))

    suspend fun close() {
        LOGGER.info { "Closing websocket connection on shard $shardId" }
        mutex.withLock {
            isConnected = false
        }
    }

    private suspend fun handleRawEvent(payload: Payload) = coroutineScope {
        client.handleEvent(RawEvent(client, payload))
        val data = payload.eventData!!.jsonObject
        launch {
            val event = when (payload.eventName!!) {
                "READY" -> ReadyEventHandler(client).handle(data)

                //guild events
                "GUILD_CREATE" -> GuildCreateEventHandler(client).handle(data)
                "GUILD_UPDATE" -> GuildUpdateEventHandler(client).handle(data)
                "GUILD_DELETE" -> GuildDeleteEventHandler(client, LOGGER).handle(data)
                "GUILD_BAN_ADD" -> BanEventHandler(client).handle<GuildBanAddEvent>(data)
                "GUILD_BAN_REMOVE" -> BanEventHandler(client).handle<GuildBanRemoveEvent>(data)
                "GUILD_EMOJIS_UPDATE" -> GuildEmojisUpdateEventHandler(client).handle(data)
                "GUILD_STICKERS_UPDATE" -> GuildStickersUpdateEventHandler(client).handle(data)

                //stage instances
                "STAGE_INSTANCE_CREATE" -> StageInstanceCreateEventHandler(client).handle(data)
                "STAGE_INSTANCE_UPDATE" -> StageInstanceUpdateEventHandler(client).handle(data)
                "STAGE_INSTANCE_DELETE" -> StageInstanceDeleteEventHandler(client).handle(data)

                //misc
                "WEBHOOKS_UPDATE" -> WebhooksUpdateEventHandler(client).handle(data)
                "TYPING_START" -> TypingStartEventHandler(client).handle(data)
                "USER_UPDATE" -> SelfUserUpdateEventHandler(client).handle(data)
                "PRESENCE_UPDATE" -> PresenceUpdateEventHandler(client).handle(data)
                "VOICE_SERVER_UPDATE" -> VoiceServerUpdate(client, data)

                //roles
                "GUILD_ROLE_CREATE" -> RoleCreateEventHandler(client).handle(data)
                "GUILD_ROLE_UPDATE" -> RoleUpdateEventHandler(client).handle(data)
                "GUILD_ROLE_DELETE" -> RoleDeleteEventHandler(client).handle(data)

                //scheduled events
                "GUILD_SCHEDULED_EVENT_CREATE" -> ScheduledEventCreateHandler(client).handle(data)
                "GUILD_SCHEDULED_EVENT_UPDATE" -> ScheduledEventUpdateHandler(client).handle(data)
                "GUILD_SCHEDULED_EVENT_DELETE" -> ScheduledEventDeleteHandler(client).handle(data)
                "GUILD_SCHEDULED_EVENT_USER_ADD" -> ScheduledEventUserAddEventHandler(client).handle(data)
                "GUILD_SCHEDULED_EVENT_USER_REMOVE" -> ScheduledEventUserRemoveEventHandler(client).handle(data)

                //invites
                "INVITE_CREATE" -> InviteCreateEventHandler(client).handle(data)
                "INVITE_DELETE" -> InviteDeleteEventHandler(client).handle(data)

                //interactions
                "INTERACTION_CREATE" -> InteractionCreateEventHandler(client).handle(data)

                //channels
                "CHANNEL_CREATE" -> ChannelCreateEventHandler(client).handle(data)
                "CHANNEL_UPDATE" -> ChannelUpdateEventHandler(client).handle(data)
                "CHANNEL_DELETE" -> ChannelDeleteEventHandler(client).handle(data)
                "CHANNEL_PINS_UPDATE" -> ChannelPinUpdateEventHandler(client).handle(data)

                //members
                "GUILD_MEMBER_ADD" -> GuildMemberAddEventHandler(client).handle(data)
                "GUILD_MEMBER_UPDATE" -> GuildMemberUpdateEventHandler(client).handle(data)
                "GUILD_MEMBER_REMOVE" -> GuildMemberRemoveEventHandler(client).handle(data)

                //threads
                "THREAD_CREATE" -> ThreadCreateEventHandler(client).handle(data)
                "THREAD_UPDATE" -> ThreadUpdateEventHandler(client).handle(data)
                "THREAD_DELETE" -> ThreadDeleteEventHandler(client).handle(data)
                "THREAD_MEMBERS_UPDATE" -> ThreadMembersUpdateEventHandler(client).handle(data)

                //message events
                "MESSAGE_CREATE" -> MessageCreateEventHandler(client).handle(data)
                "MESSAGE_UPDATE" -> MessageUpdateEventHandler(client).handle(data)
                "MESSAGE_DELETE" -> MessageDeleteEventHandler(client).handle(data)
                "MESSAGE_DELETE_BULK" -> MessageBulkDeleteEventHandler(client).handle(data)
                "MESSAGE_REACTION_ADD" -> MessageReactionAddEventHandler(client).handle(data)
                "MESSAGE_REACTION_REMOVE" -> MessageReactionRemoveEventHandler(client).handle(data)
                "MESSAGE_REACTION_REMOVE_ALL" -> MessageReactionRemoveAllEventHandler(client).handle(data)
                "MESSAGE_REACTION_REMOVE_EMOJI" -> MessageReactionEmojiRemoveEventHandler(client).handle(data)

                //voice states
                "VOICE_STATE_UPDATE" -> VoiceStateUpdateEventHandler(client).handle(data)
                else -> return@launch
            }
            client.handleEvent(event)
        }
    }

    enum class OpCode(val code: Int) {
        DISPATCH(0),
        HEARTBEAT(1),
        INVALID_SESSION(9),
        HELLO(10),
        RECONNECT(7),
        HEARTBEAT_ACK(11);

        companion object {

            fun fromCode(code: Int) = values().first { it.code == code }

        }
    }

}

typealias Task = Payload