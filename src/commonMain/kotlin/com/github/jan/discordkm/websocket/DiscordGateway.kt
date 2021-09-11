package com.github.jan.discordkm.websocket

import com.soywiz.klogger.Logger
import com.soywiz.korio.net.ws.WebSocketClient
import com.soywiz.korio.net.ws.WsCloseInfo
import com.github.jan.discordkm.DiscordClient
import com.github.jan.discordkm.events.internal.GuildCreateEventHandler
import com.github.jan.discordkm.events.internal.ReadyEventHandler
import com.github.jan.discordkm.serialization.IdentifyPayload
import com.github.jan.discordkm.serialization.Payload
import com.github.jan.discordkm.serialization.send
import com.github.jan.discordkm.utils.LoggerOutput
import com.github.jan.discordkm.utils.generateWebsocketURL
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

    private suspend fun handleRawEvent(payload: Payload) {
      //  println(payload.eventData!!)
        val event = when(payload.eventName!!) {
            "READY" -> ReadyEventHandler(client).handle(payload.eventData!!)
            "GUILD_CREATE" -> GuildCreateEventHandler(client).handle(payload.eventData!!)
            else -> return
        }
        client.handleEvent(event)
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