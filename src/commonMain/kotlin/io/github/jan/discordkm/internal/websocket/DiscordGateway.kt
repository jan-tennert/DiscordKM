/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.websocket

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import com.soywiz.klogger.Logger
import com.soywiz.korio.net.ws.WebSocketClient
import com.soywiz.korio.net.ws.WsCloseInfo
import io.github.jan.discordkm.api.entities.activity.Presence
import io.github.jan.discordkm.api.entities.activity.PresenceStatus
import io.github.jan.discordkm.api.entities.clients.ClientConfig
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.clients.Intent
import io.github.jan.discordkm.api.entities.clients.WSDiscordClient
import io.github.jan.discordkm.api.entities.clients.WSDiscordClientImpl
import io.github.jan.discordkm.api.events.GuildBanAddEvent
import io.github.jan.discordkm.api.events.GuildBanRemoveEvent
import io.github.jan.discordkm.api.events.RawEvent
import io.github.jan.discordkm.api.events.ResumeEvent
import io.github.jan.discordkm.api.events.VoiceServerUpdate
import io.github.jan.discordkm.internal.events.ApplicationCommandsPermissionsUpdateEventHandler
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
import io.github.jan.discordkm.internal.events.GuildMembersChunkEventHandler
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
import io.github.jan.discordkm.internal.events.ThreadListSyncEventHandler
import io.github.jan.discordkm.internal.events.ThreadMembersUpdateEventHandler
import io.github.jan.discordkm.internal.events.ThreadUpdateEventHandler
import io.github.jan.discordkm.internal.events.TypingStartEventHandler
import io.github.jan.discordkm.internal.events.VoiceStateUpdateEventHandler
import io.github.jan.discordkm.internal.events.WebhooksUpdateEventHandler
import io.github.jan.discordkm.internal.serialization.IdentifyPayload
import io.github.jan.discordkm.internal.serialization.Payload
import io.github.jan.discordkm.internal.serialization.rawValue
import io.github.jan.discordkm.internal.serialization.send
import io.github.jan.discordkm.internal.utils.LoggerConfig
import io.github.jan.discordkm.internal.utils.generateWebsocketURL
import io.github.jan.discordkm.internal.utils.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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
import kotlin.coroutines.coroutineContext

class DiscordGateway internal constructor(
    private val config: ClientConfig,
    val client: WSDiscordClient,
    private val shardId: Int = 0,
) {

    internal val LOGGER = config.map<LoggerConfig>("logging")("Websocket")
    private var heartbeatInterval = 0L
    private var lastSequenceNumber: Int? = null
    var sessionId: String? = null
        internal set
    val mutex = Mutex()
    private var resumeTries = 0
    private var heartbeatSent = 0
    private var heartbeatReceived = 0
    private var authenticated = false
    private lateinit var socket: WebSocketClient
    var isConnected = false
        private set
    private lateinit var heartbeatTask: Job
    private lateinit var heartbeatWatcher: Job

    suspend fun start(delay: Boolean = false) {
        if(isConnected) return
        if (delay) com.soywiz.korio.async.delay(config.map<TimeSpan>("reconnectDelay"))
        LOGGER.log(true, Logger.Level.INFO) { "Connecting to gateway..." }
        try {
            socket = WebSocketClient(generateWebsocketURL(
                config.map("encoding"),
                config.map("compression")
            ))
            mutex.withLock { isConnected = true; authenticated = false }
           /* with(socket) {
                LOGGER.log(true, Logger.Level.INFO) { "Connected to gateway!" }
                while (isConnected) {
                    try {
                        val message = incoming.receive().readBytes().decodeToString()
                        onMessage(message)
                    } catch (e: Exception) {
                        mutex.withLock { isConnected = false }
                        val reason = closeReason.await()!!
                        ErrorHandler.handle(reason, LOGGER, config.map("reconnectDelay"))
                        launch(kotlin.coroutines.coroutineContext) { start(delay = true) }
                    }
                    com.soywiz.korio.async.delay(100.milliseconds)
                }
            }*/
            socket.onClose {
                LOGGER.info { "Closed websocket connection on shard $shardId. Reason: ${it.message}, Code: ${it.code}" }
            }
            socket.onError {
                attemptToReconnect(it)
            }
            socket.onStringMessage {
                onMessage(it)
            }
        } catch (e: Exception) {
            mutex.withLock { isConnected = false }
            LOGGER.log(true, Logger.Level.ERROR) { "Failed to connect to the gateway!. Retrying in ${config.map<Any>("reconnectDelay")}..." }
            com.soywiz.korio.async.launch(coroutineContext) { start(delay = true) }
        }
        mutex.withLock{ isConnected = false }
    }

    suspend fun send(payload: Payload) {
        socket.send(payload)
    }

    private fun attemptToReconnect(error: Throwable) {
        LOGGER.error { "Error on gateway: ${error.stackTraceToString()}. Attempting to reconnect..." }
        com.soywiz.korio.async.launch(Dispatchers.Default) {
            close()
            start(true)
        }
    }

    private fun onMessage(message: String) {
        val json = Json.parseToJsonElement(message).jsonObject
        var data = json["d"]
        if (data is JsonNull) data = null
        if (data.toString() == "false") data = null
        com.soywiz.korio.async.launch(Dispatchers.Default) {
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
                mutex.withLock {
                    lastSequenceNumber = payload.sequenceNumber
                    resumeTries = 0
                    authenticated = true
                }
                payload.eventName?.let { LOGGER.debug { "Received event $it on shard $shardId" } }
                client.handleRawEvent(payload, LOGGER)
            }
            OpCode.HEARTBEAT -> {
                sendHeartbeat()
            }
            OpCode.RECONNECT -> {
                close()
                LOGGER.info { "Received opcode RECONNECT, reconnecting..." }
                start(true)
            }
            OpCode.INVALID_SESSION -> {
                LOGGER.warn { "Invalid session, reconnecting..." }
                close()
                this@DiscordGateway.start(true)
            }
            OpCode.HELLO -> {
                heartbeatInterval = payload.eventData!!.jsonObject["heartbeat_interval"]!!.jsonPrimitive.long
                coroutineScope {
                    heartbeatTask = launch {
                        LOGGER.debug { "Start heartbeating..." }
                        startHeartbeating()
                    }
                    heartbeatWatcher = launch {
                        LOGGER.debug { "Start heartbeat watcher..." }
                        startHeartbeatWatcher()
                    }
                    launch {
                        if (sessionId != null && resumeTries < config.map<Int>("maxResumeTries")) {
                            val tryMessage =
                                if (resumeTries == 0) "First try" else if (resumeTries == 1) "Second try" else if (resumeTries == 2) "Third try" else "${resumeTries - 1}th try"
                            mutex.withLock { resumeTries++ }
                            LOGGER.info { "Trying to resume... ($tryMessage)" }
                            send(Payload(6, buildJsonObject {
                                put("token", config.map<String>("token"))
                                put("session_id", sessionId)
                                put("seq", lastSequenceNumber)
                            }))
                        } else {
                            sessionId = null
                            lastSequenceNumber = null
                            resumeTries = 0
                            LOGGER.debug { "Authenticate..." }
                            send(
                                IdentifyPayload(
                                    config.map<String>("token"),
                                    config.map<Set<Intent>>("intents").rawValue(),
                                    config.map<PresenceStatus>("status"),
                                    config.map<Presence>("activity"),
                                    shardId,
                                    config.map<Int>("totalShards")
                                )
                            )
                        }
                    }
                }
            }
            OpCode.HEARTBEAT_ACK -> {
                LOGGER.debug { "Received heartbeat acknowledge" }
                mutex.withLock { heartbeatReceived++ }
            }
        }
    }

    private suspend fun CoroutineScope.startHeartbeatWatcher() {
        while(isActive) {
            com.soywiz.korio.async.delay(10.seconds)
            if(heartbeatSent > heartbeatReceived && isActive) {
                com.soywiz.korio.async.delay(30.seconds)
                if(heartbeatSent == heartbeatReceived || !isActive) continue
                LOGGER.warn { "No heartbeat response received, reconnecting in ${config.get("reconnectDelay")}..." }
                close()
                start(true)
                break
            }
        }
    }

    private suspend fun CoroutineScope.startHeartbeating() {
        while (isActive) {
            delay(heartbeatInterval)
            if (!isActive) return
            LOGGER.debug { "Sending heartbeat..." }
            sendHeartbeat()
            mutex.withLock { heartbeatSent++ }
        }
    }

    private suspend fun sendHeartbeat() = send(Payload(1, JsonPrimitive(heartbeatInterval), lastSequenceNumber, null))

    suspend fun close() {
        LOGGER.warn { "Closing websocket connection on shard $shardId" }
        if(::heartbeatTask.isInitialized) heartbeatTask.cancel()
        if(::heartbeatWatcher.isInitialized) heartbeatWatcher.cancel()
        socket.close(WsCloseInfo(1000, "Normal closure"))
        mutex.withLock {
            heartbeatSent = 0
            heartbeatReceived = 0
            isConnected = false
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

suspend fun DiscordClient.handleRawEvent(payload: Payload, LOGGER: Logger) = coroutineScope {
    (client as WSDiscordClientImpl).handleEvent(RawEvent(client, payload))
    val data = payload.eventData!!.jsonObject
    launch {
        val event = when (payload.eventName!!) {
            "READY" -> ReadyEventHandler(client).handle(data)
            "RESUMED" -> ResumeEvent(client)

            //guild events
            "GUILD_CREATE" -> GuildCreateEventHandler(client).handle(data)
            "GUILD_UPDATE" -> GuildUpdateEventHandler(client).handle(data)
            "GUILD_DELETE" -> GuildDeleteEventHandler(client, LOGGER).handle(data)
            "GUILD_BAN_ADD" -> BanEventHandler(client).handle<GuildBanAddEvent>(data)
            "GUILD_BAN_REMOVE" -> BanEventHandler(client).handle<GuildBanRemoveEvent>(data)
            "GUILD_EMOJIS_UPDATE" -> GuildEmojisUpdateEventHandler(client).handle(data)
            "GUILD_STICKERS_UPDATE" -> GuildStickersUpdateEventHandler(client).handle(data)
            "GUILD_MEMBERS_CHUNK" -> GuildMembersChunkEventHandler(client).handle(data)

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

            //commands
            "APPLICATION_COMMAND_PERMISSIONS_UPDATE" -> ApplicationCommandsPermissionsUpdateEventHandler(client).handle(data)

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
            "THREAD_LIST_SYNC" -> ThreadListSyncEventHandler(client).handle(data)

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
        runCatching {
            (client as WSDiscordClientImpl).handleEvent(event)
        }.onFailure {
            LOGGER.error { "Error while handling event ${event::class.simpleName}: ${it.stackTraceToString()}" }
        }
    }
}
