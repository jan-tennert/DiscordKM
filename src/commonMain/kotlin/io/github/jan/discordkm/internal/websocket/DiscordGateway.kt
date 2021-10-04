/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.websocket

import com.soywiz.klock.TimeSpan
import com.soywiz.klogger.Logger
import io.github.jan.discordkm.api.entities.activity.Presence
import io.github.jan.discordkm.api.entities.activity.PresenceStatus
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.events.GuildBanAddEvent
import io.github.jan.discordkm.api.events.GuildBanRemoveEvent
import io.github.jan.discordkm.api.events.RawEvent
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
import io.github.jan.discordkm.internal.serialization.send
import io.github.jan.discordkm.internal.utils.LoggerOutput
import io.github.jan.discordkm.internal.utils.generateWebsocketURL
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.webSocketSession
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.readBytes
import io.ktor.http.cio.websocket.send
import io.ktor.http.takeFrom
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put

class DiscordGateway(
    val encoding: Encoding,
    val compression: Compression,
    val client: DiscordWebSocketClient,
    val status: PresenceStatus,
    val activity: Presence?,
    val reconnectDelay: TimeSpan,
    val shardId: Int = 0,
    val totalShards: Int = -1
) {

    private val LOGGER = Logger("Websocket")
    private var heartbeatInterval = 0L
    private var lastSequenceNumber: Int? = null
    var sessionId: String? = null
        internal set
    var isClosed = true
        private set
    private val http = HttpClient() {
        install(WebSockets)
    }
    lateinit var ws: DefaultClientWebSocketSession

    init {
        LOGGER.level = client.loggingLevel
        LOGGER.output = LoggerOutput
    }

    suspend fun start(normalStart: Boolean = true) {
        isClosed = false
        if(sessionId != null || !normalStart) com.soywiz.korio.async.delay(reconnectDelay)
        LOGGER.info { "Connecting to gateway..." }
        ws = http.webSocketSession() {
            method = HttpMethod.Get
            url.takeFrom(generateWebsocketURL(encoding, compression))
        }

        LOGGER.info { "Connected to gateway!" }
        while(true) {
            try {
                val message = ws.incoming.receive().readBytes().decodeToString()
                onMessage(message)
            } catch(_: Exception) {
                LOGGER.error { "Disconnected due to an error: ${ws.closeReason.await()}. Trying to reconnect in ${reconnectDelay.seconds} seconds" }
                client.launch { start(false) }
                break
            }

        }
        /*ws.onError {

        }*/
    }

    suspend fun send(payload: Payload) = ws.send(payload)

    private fun onMessage(message: String) {
        val json = Json.parseToJsonElement(message).jsonObject
        var data = json["d"]
        if(data is JsonNull) data = null
        if(data.toString() == "false") data = null
        client.launch {
            onEvent(Payload(
                json.getValue("op").jsonPrimitive.int,
                data?.jsonObject,
                json["s"]?.jsonPrimitive?.intOrNull,
                json["t"]?.jsonPrimitive?.content
            ))
        }
    }

    private suspend fun onEvent(payload: Payload) {
        when(OpCode.fromCode(payload.opCode)) {
            OpCode.DISPATCH -> {
                payload.eventName?.let { LOGGER.debug { "Received event $it" } }
                lastSequenceNumber = payload.sequenceNumber
                handleRawEvent(payload)
            }
            OpCode.HEARTBEAT -> {
                sendHeartbeat()
            }
            OpCode.INVALID_SESSION -> {
                LOGGER.warn { "Failed to resume! Trying to reconnect manually..." }
                close()
                sessionId = null
                isClosed = true
                this@DiscordGateway.start(false)
            }
            OpCode.HELLO -> {
                heartbeatInterval = payload.eventData!!["heartbeat_interval"]!!.jsonPrimitive.long
                coroutineScope {
                    launch {
                        LOGGER.debug { "Start heartbeating..." }
                        startHeartbeating()
                    }
                    launch {
                        if(sessionId != null) {
                            LOGGER.info { "Trying to resume..." }
                            send(Payload(6, buildJsonObject {
                                put("token", client.token)
                                put("session_id", sessionId)
                                put("seq", lastSequenceNumber)
                            }))
                        } else {
                            LOGGER.debug { "Authenticate..." }
                            send(IdentifyPayload(client.token, client.intents.rawValue, status, activity, shardId, totalShards))
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
        while(true) {
            if(isClosed) return
            delay(heartbeatInterval)
            sendHeartbeat()
            LOGGER.debug { "Sending heartbeat..." }
        }
    }

  private suspend fun sendHeartbeat() {
        ws.send(buildJsonObject {
            put("op", 1)
            put("d", heartbeatInterval)
            put("s", lastSequenceNumber)
        }.toString())
    }

    fun close() {
        http.close()
    }

    private suspend fun handleRawEvent(payload: Payload) = coroutineScope {
        client.handleEvent(RawEvent(client, payload))
        launch {
            val event = when(payload.eventName!!) {
                "READY" -> ReadyEventHandler(client).handle(payload.eventData!!)

                //guild events
                "GUILD_CREATE" -> GuildCreateEventHandler(client).handle(payload.eventData!!)
                "GUILD_UPDATE" -> GuildUpdateEventHandler(client).handle(payload.eventData!!)
                "GUILD_DELETE" -> GuildDeleteEventHandler(client, LOGGER).handle(payload.eventData!!)
                "GUILD_BAN_ADD" -> BanEventHandler(client).handle<GuildBanAddEvent>(payload.eventData!!)
                "GUILD_BAN_REMOVE" -> BanEventHandler(client).handle<GuildBanRemoveEvent>(payload.eventData!!)
                "GUILD_EMOJIS_UPDATE" -> GuildEmojisUpdateEventHandler(client).handle(payload.eventData!!)
                "GUILD_STICKERS_UPDATE" -> GuildStickersUpdateEventHandler(client).handle(payload.eventData!!)

                //stage instances
                "STAGE_INSTANCE_CREATE" -> StageInstanceCreateEventHandler(client).handle(payload.eventData!!)
                "STAGE_INSTANCE_UPDATE" -> StageInstanceUpdateEventHandler(client).handle(payload.eventData!!)
                "STAGE_INSTANCE_DELETE" -> StageInstanceDeleteEventHandler(client).handle(payload.eventData!!)

                //misc
                "WEBHOOKS_UPDATE" -> WebhooksUpdateEventHandler(client).handle(payload.eventData!!)
                "TYPING_START" -> TypingStartEventHandler(client).handle(payload.eventData!!)
                "USER_UPDATE" -> SelfUserUpdateEventHandler(client).handle(payload.eventData!!)
                "PRESENCE_UPDATE" -> PresenceUpdateEventHandler(client).handle(payload.eventData!!)

                //roles
                "GUILD_ROLE_CREATE" -> RoleCreateEventHandler(client).handle(payload.eventData!!)
                "GUILD_ROLE_UPDATE" -> RoleUpdateEventHandler(client).handle(payload.eventData!!)
                "GUILD_ROLE_DELETE" -> RoleDeleteEventHandler(client).handle(payload.eventData!!)

                //invites
                "INVITE_CREATE" -> InviteCreateEventHandler(client).handle(payload.eventData!!)
                "INVITE_DELETE" -> InviteDeleteEventHandler(client).handle(payload.eventData!!)

                //interactions
                "INTERACTION_CREATE" -> InteractionCreateEventHandler(client).handle(payload.eventData!!)

                //channels
                "CHANNEL_CREATE" -> ChannelCreateEventHandler(client).handle(payload.eventData!!)
                "CHANNEL_UPDATE" -> ChannelUpdateEventHandler(client).handle(payload.eventData!!)
                "CHANNEL_DELETE" -> ChannelDeleteEventHandler(client).handle(payload.eventData!!)
                "CHANNEL_PINS_UPDATE" -> ChannelPinUpdateEventHandler(client).handle(payload.eventData!!)

                //members
                "GUILD_MEMBER_ADD" -> GuildMemberAddEventHandler(client).handle(payload.eventData!!)
                "GUILD_MEMBER_UPDATE" -> GuildMemberUpdateEventHandler(client).handle(payload.eventData!!)
                "GUILD_MEMBER_REMOVE" -> GuildMemberRemoveEventHandler(client).handle(payload.eventData!!)

                //threads
                "THREAD_CREATE" -> ThreadCreateEventHandler(client).handle(payload.eventData!!)
                "THREAD_UPDATE" -> ThreadUpdateEventHandler(client).handle(payload.eventData!!)
                "THREAD_DELETE" -> ThreadDeleteEventHandler(client).handle(payload.eventData!!)
                "THREAD_MEMBERS_UPDATE" -> ThreadMembersUpdateEventHandler(client).handle(payload.eventData!!)

                //message events
                "MESSAGE_CREATE" -> MessageCreateEventHandler(client).handle(payload.eventData!!)
                "MESSAGE_UPDATE" -> MessageUpdateEventHandler(client).handle(payload.eventData!!)
                "MESSAGE_DELETE" -> MessageDeleteEventHandler(client).handle(payload.eventData!!)
                "MESSAGE_DELETE_BULK" -> MessageBulkDeleteEventHandler(client).handle(payload.eventData!!)
                "MESSAGE_REACTION_ADD" -> MessageReactionAddEventHandler(client).handle(payload.eventData!!)
                "MESSAGE_REACTION_REMOVE" -> MessageReactionRemoveEventHandler(client).handle(payload.eventData!!)
                "MESSAGE_REACTION_REMOVE_ALL" -> MessageReactionRemoveAllEventHandler(client).handle(payload.eventData!!)
                "MESSAGE_REACTION_REMOVE_EMOJI" -> MessageReactionEmojiRemoveEventHandler(client).handle(payload.eventData!!)

                //voice states
                "VOICE_STATE_UPDATE" -> VoiceStateUpdateEventHandler(client).handle(payload.eventData!!)
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
        HEARTBEAT_ACK(11);

        companion object {

            fun fromCode(code: Int) = values().first { it.code == code }

        }
    }

}

typealias WebSocketAction = suspend () -> String