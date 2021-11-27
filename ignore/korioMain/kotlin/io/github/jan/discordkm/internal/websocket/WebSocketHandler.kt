package io.github.jan.discordkm.internal.websocket

import com.soywiz.klock.TimeSpan
import com.soywiz.klogger.Logger
import com.soywiz.korio.net.ws.WebSocketClient
import io.github.jan.discordkm.api.entities.activity.Presence
import io.github.jan.discordkm.api.entities.activity.PresenceStatus
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.internal.serialization.Payload
import io.github.jan.discordkm.internal.utils.LoggerOutput
import io.github.jan.discordkm.internal.utils.generateWebsocketURL
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual abstract class WebSocketHandler {

    actual var heartbeatInterval = 0L
    actual var lastSequenceNumber: Int? = null
    actual var sessionId: String? = null
    actual var isClosed: Boolean = true
    actual var disconnect: Boolean = false

    private lateinit var ws: WebSocketClient
    private val LOGGER = Logger("Websocket")

    init {
        LOGGER.output = LoggerOutput
    }

    actual abstract val encoding: Encoding
    actual abstract val compression: Compression
    actual abstract val client: DiscordWebSocketClient
    actual abstract val status: PresenceStatus
    actual abstract val activity: Presence?
    actual abstract val reconnectDelay: TimeSpan
    actual abstract val shardId: Int
    actual abstract val totalShards: Int

    actual suspend fun start(normalStart: Boolean) {
        LOGGER.level = client.loggingLevel
        isClosed = false
        if(sessionId != null || !normalStart) com.soywiz.korio.async.delay(reconnectDelay)
        LOGGER.info { "Connecting to gateway..." }
        ws = WebSocketClient(generateWebsocketURL(encoding, compression))
        ws.onStringMessage {
            println(it)
            onMessage(it)
        }
        ws.onOpen { LOGGER.info { "Connected to gateway!" } }
        ws.onError {
            println("error $it")
            isClosed = true
            if(it.toString().contains("StandaloneCoroutine was cancelled")) return@onError
            LOGGER.error { "Disconnected due to an error: ${it.message}. Trying to reconnect in ${reconnectDelay.seconds} seconds" }
            client.launch { start(false) }
        }
        ws.onBinaryMessage {
            //zlib and eft
        }
        ws.onClose {
            println("close")
            if(it.message != null) {
                LOGGER.error { "Disconnected from gateway. Reason: ${it.message}. Trying to reconnect in ${reconnectDelay.seconds} seconds" }
                client.launch { start(false) }
            } else {
                LOGGER.info { "Connection closed!" }
                isClosed = true
            }
        }
    }

    actual suspend fun close() {
        LOGGER.info { "Closing websocket connection on shard $shardId" }
        isClosed = true
        disconnect = true
        ws.close(0, "Closed by user")
    }

    actual suspend fun send(payload: Payload)  = ws.send(Json.encodeToString(payload))

    actual abstract fun onMessage(message: String)

}