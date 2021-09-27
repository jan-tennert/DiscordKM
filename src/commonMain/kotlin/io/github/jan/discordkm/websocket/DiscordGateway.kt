/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.websocket

import com.soywiz.klock.TimeSpan
import com.soywiz.klogger.Logger
import com.soywiz.korio.net.ws.WebSocketClient
import com.soywiz.korio.net.ws.WsCloseInfo
import io.github.jan.discordkm.api.entities.activity.DiscordActivity
import io.github.jan.discordkm.api.entities.activity.PresenceStatus
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.events.GuildBanAddEvent
import io.github.jan.discordkm.api.events.GuildBanRemoveEvent
import io.github.jan.discordkm.internal.events.internal.BanEventHandler
import io.github.jan.discordkm.internal.events.internal.ChannelCreateEventHandler
import io.github.jan.discordkm.internal.events.internal.ChannelDeleteEventHandler
import io.github.jan.discordkm.internal.events.internal.ChannelUpdateEventHandler
import io.github.jan.discordkm.internal.events.internal.GuildCreateEventHandler
import io.github.jan.discordkm.internal.events.internal.GuildDeleteEventHandler
import io.github.jan.discordkm.internal.events.internal.GuildEmojisUpdateEventHandler
import io.github.jan.discordkm.internal.events.internal.GuildStickersUpdateEventHandler
import io.github.jan.discordkm.internal.events.internal.GuildUpdateEventHandler
import io.github.jan.discordkm.internal.events.internal.InteractionCreateEventHandler
import io.github.jan.discordkm.internal.events.internal.MessageBulkDeleteEventHandler
import io.github.jan.discordkm.internal.events.internal.MessageCreateEventHandler
import io.github.jan.discordkm.internal.events.internal.MessageDeleteEventHandler
import io.github.jan.discordkm.internal.events.internal.MessageReactionAddEventHandler
import io.github.jan.discordkm.internal.events.internal.MessageReactionEmojiRemoveEventHandler
import io.github.jan.discordkm.internal.events.internal.MessageReactionRemoveAllEventHandler
import io.github.jan.discordkm.internal.events.internal.MessageReactionRemoveEventHandler
import io.github.jan.discordkm.internal.events.internal.MessageUpdateEventHandler
import io.github.jan.discordkm.internal.events.internal.ReadyEventHandler
import io.github.jan.discordkm.internal.events.internal.VoiceStateUpdateEventHandler
import io.github.jan.discordkm.internal.serialization.IdentifyPayload
import io.github.jan.discordkm.internal.serialization.Payload
import io.github.jan.discordkm.internal.serialization.send
import io.github.jan.discordkm.internal.utils.LoggerOutput
import io.github.jan.discordkm.internal.utils.generateWebsocketURL
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
    val client: DiscordClient,
    val status: PresenceStatus,
    val activity: DiscordActivity?,
    val reconnectDelay: TimeSpan,
) {

    lateinit var ws: WebSocketClient
    private val LOGGER = Logger("Websocket")
    private var heartbeatInterval = 0L
    private var lastSequenceNumber: Int? = null
    internal var sessionId: String? = null
    private var closed = true

    init {
        LOGGER.level = client.loggingLevel
        LOGGER.output = LoggerOutput
    }

    suspend fun start() {
        LOGGER.info { "Connecting to gateway..." }
        closed = false
        if(sessionId != null) com.soywiz.korio.async.delay(reconnectDelay)
        ws = WebSocketClient(generateWebsocketURL(encoding, compression))
        ws.onStringMessage {
            val json = Json.parseToJsonElement(it).jsonObject
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
        ws.onOpen { LOGGER.info { "Connected to gateway!" } }
        ws.onError {
            if(it.toString().contains("StandaloneCoroutine was cancelled")) return@onError
            LOGGER.error { "Disconnected due to an error: ${it.message}. Trying to reconnect in ${reconnectDelay.seconds} seconds" }
            client.launch { start() }
        }
        ws.onClose {
            if(it.message != null) {
                LOGGER.error { "Disconnected from gateway. Reason: ${it.message}. Trying to reconnect in ${reconnectDelay.seconds} seconds" }
                client.launch { start() }
            } else {
                LOGGER.info { "Connection closed!" }
                closed = true
            }
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
            OpCode.RECONNECT -> TODO()
            OpCode.INVALID_SESSION -> {
                LOGGER.warn { "Failed to resume! Trying to reconnect manually..." }
                ws.close()
                sessionId = null
                closed = true
                start()
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
                            ws.send(Payload(6, buildJsonObject {
                                put("token", client.token)
                                put("session_id", sessionId)
                                put("seq", lastSequenceNumber)
                            }))
                        } else {
                            LOGGER.debug { "Authenticate..." }
                            ws.send(IdentifyPayload(client.token, client.intents.rawValue, status, activity))
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
            if(closed) return
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

    suspend fun send(payload: Payload) {
        ws.send(payload)
    }

    fun close() {
        ws.close(WsCloseInfo.NormalClosure)
    }

    private suspend fun handleRawEvent(payload: Payload) = coroutineScope {
        println(payload.eventData)
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

                //interactions
                "INTERACTION_CREATE" -> InteractionCreateEventHandler(client).handle(payload.eventData!!)

                //channels
                "CHANNEL_CREATE" -> ChannelCreateEventHandler(client).handle(payload.eventData!!)
                "CHANNEL_UPDATE" -> ChannelUpdateEventHandler(client).handle(payload.eventData!!)
                "CHANNEL_DELETE" -> ChannelDeleteEventHandler(client).handle(payload.eventData!!)

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
        RECONNECT(7),
        INVALID_SESSION(9),
        HELLO(10),
        HEARTBEAT_ACK(11);

        companion object {

            fun fromCode(code: Int) = values().first { it.code == code }

        }
    }

}