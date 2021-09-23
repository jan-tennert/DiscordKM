/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.websocket

import com.soywiz.klogger.Logger
import com.soywiz.korio.net.ws.WebSocketClient
import com.soywiz.korio.net.ws.WsCloseInfo
import io.github.jan.discordkm.clients.DiscordClient
import io.github.jan.discordkm.events.GuildBanAddEvent
import io.github.jan.discordkm.events.GuildBanRemoveEvent
import io.github.jan.discordkm.events.internal.BanEventHandler
import io.github.jan.discordkm.events.internal.GuildCreateEventHandler
import io.github.jan.discordkm.events.internal.GuildDeleteEventHandler
import io.github.jan.discordkm.events.internal.GuildEmojisUpdateEventHandler
import io.github.jan.discordkm.events.internal.GuildStickersUpdateEventHandler
import io.github.jan.discordkm.events.internal.GuildUpdateEventHandler
import io.github.jan.discordkm.events.internal.InteractionCreateEventHandler
import io.github.jan.discordkm.events.internal.MessageBulkDeleteEventHandler
import io.github.jan.discordkm.events.internal.MessageCreateEventHandler
import io.github.jan.discordkm.events.internal.MessageDeleteEventHandler
import io.github.jan.discordkm.events.internal.MessageReactionAddEventHandler
import io.github.jan.discordkm.events.internal.MessageReactionEmojiRemoveEventHandler
import io.github.jan.discordkm.events.internal.MessageReactionRemoveAllEventHandler
import io.github.jan.discordkm.events.internal.MessageReactionRemoveEventHandler
import io.github.jan.discordkm.events.internal.MessageUpdateEventHandler
import io.github.jan.discordkm.events.internal.ReadyEventHandler
import io.github.jan.discordkm.serialization.IdentifyPayload
import io.github.jan.discordkm.serialization.Payload
import io.github.jan.discordkm.serialization.send
import io.github.jan.discordkm.utils.LoggerOutput
import io.github.jan.discordkm.utils.generateWebsocketURL
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

class DiscordGateway(val encoding: Encoding, val compression: Compression, val client: DiscordClient) {

    lateinit var ws: WebSocketClient
    private val LOGGER = Logger("Websocket")
    private var heartbeatInterval = 0L
    private var lastSequenceNumber: Int? = null
    internal var sessionId: String? = null

    init {
        LOGGER.level = client.loggingLevel
        LOGGER.output = LoggerOutput
    }

    suspend fun start() {
        ws = WebSocketClient(generateWebsocketURL(encoding, compression))
        ws.onStringMessage {
            val json = Json.parseToJsonElement(it).jsonObject
            println(json)
            var data = json["d"]
            if(data is JsonNull) data = null
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
            LOGGER.error { "Gateway error: $it" }
        }
        ws.onClose {
            LOGGER.info { "Connection closed!" }
        }
    }

    private suspend fun onEvent(payload: Payload) {
        when(OpCode.fromCode(payload.opCode)) {
            OpCode.DISPATCH -> {
                payload.eventName?.let { LOGGER.debug { "Received event $it" } }
                println(payload)
                handleRawEvent(payload)
            }
            OpCode.HEARTBEAT -> {
                sendHeartbeat()
            }
            OpCode.RECONNECT -> TODO()
            OpCode.INVALID_SESSION -> TODO()
            OpCode.HELLO -> {
                heartbeatInterval = payload.eventData!!["heartbeat_interval"]!!.jsonPrimitive.long
                coroutineScope {
                    launch {
                        LOGGER.debug { "Start heartbeating..." }
                        startHeartbeating()
                    }
                    launch {
                        LOGGER.debug { "Authenticate..." }
                        ws.send(IdentifyPayload(client.token, client.intents.rawValue))
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
        ws.close(WsCloseInfo.NormalClosure)
    }

    private suspend fun handleRawEvent(payload: Payload) = coroutineScope {
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

                //message events
                "MESSAGE_CREATE" -> MessageCreateEventHandler(client).handle(payload.eventData!!)
                "MESSAGE_UPDATE" -> MessageUpdateEventHandler(client).handle(payload.eventData!!)
                "MESSAGE_DELETE" -> MessageDeleteEventHandler(client).handle(payload.eventData!!)
                "MESSAGE_DELETE_BULK" -> MessageBulkDeleteEventHandler(client).handle(payload.eventData!!)
                "MESSAGE_REACTION_ADD" -> MessageReactionAddEventHandler(client).handle(payload.eventData!!)
                "MESSAGE_REACTION_REMOVE" -> MessageReactionRemoveEventHandler(client).handle(payload.eventData!!)
                "MESSAGE_REACTION_REMOVE_ALL" -> MessageReactionRemoveAllEventHandler(client).handle(payload.eventData!!)
                "MESSAGE_REACTION_REMOVE_EMOJI" -> MessageReactionEmojiRemoveEventHandler(client).handle(payload.eventData!!)
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