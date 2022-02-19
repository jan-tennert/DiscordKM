package io.github.jan.discordkm.http

import com.soywiz.klogger.Logger
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.internal.DiscordKMInternal
import io.github.jan.discordkm.internal.utils.LoggerConfig
import io.github.jan.discordkm.internal.websocket.handleRawEvent
import io.ktor.application.call
import io.ktor.client.HttpClientConfig
import io.ktor.request.receive
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer


class HttpInteractionClient internal constructor(config: HttpConfig) : Client(config) {

    private lateinit var server: ApplicationEngine
    private val LOGGER = Logger("Http-Server")

    init {
        LOGGER.level = config.logging.level
        LOGGER.output = config.logging.output
    }

    override suspend fun disconnect() {
        server.stop(1000, 1000)
    }

    override suspend fun login() {
        val config = config as HttpConfig
        server = embeddedServer(CIO, port = config.port, host = config.host) {
            routing {
                post(config.route) {
                    handleRawEvent(this.call.receive(), LOGGER)
                }
            }
        }.start()
    }

}

class HttpInteractionClientBuilder @DiscordKMInternal constructor(var token: String) {

    var logging = LoggerConfig()
    var port = 20000
    var host = "0.0.0.0"
    var route = "/interactions"
    private var httpClientConfig: HttpClientConfig<*>.() -> Unit = {}

    @OptIn(DiscordKMInternal::class)
    fun build() = HttpInteractionClient(HttpConfig(token = token, logging = logging, httpClientConfig = httpClientConfig, port = port, host = host, route = route))

    fun httpClient(builder: HttpClientConfig<*>.() -> Unit) { httpClientConfig = builder }

}

/**
 * The HttpInteractionClient is used when you want to receive interactions over a post request rather than connecting to a websocket
 */
@OptIn(DiscordKMInternal::class)
inline fun buildHttpInteractionClient(token: String, builder: HttpInteractionClientBuilder.() -> Unit) =  HttpInteractionClientBuilder(token).apply(builder).build()
