package io.github.jan.discordkm.http

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.internal.DiscordKMInternal
import io.github.jan.discordkm.internal.utils.LoggerConfig
import io.ktor.application.call
import io.ktor.client.HttpClientConfig
import io.ktor.request.receive
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer


class HttpInteractionClient @DiscordKMInternal constructor(config: HttpConfig) : Client(config) {

    private lateinit var server: ApplicationEngine

    override suspend fun disconnect() {
        server.stop(1000, 1000)
    }

    override suspend fun login() {
        val config = config as HttpConfig
        server = embeddedServer(CIO, port = config.port, host = config.host) {
            routing {
                post(config.route) {
                    println(this.call.receive<String>())
                }
            }
        }.start()
    }

}

class HttpInteractionClientBuilder(var token: String) {

    var logging = LoggerConfig()
    var port = 20000
    var host = "0.0.0.0"
    var route = "/interactions"
    private var httpClientConfig: HttpClientConfig<*>.() -> Unit = {}

    @OptIn(DiscordKMInternal::class)
    fun build() = HttpInteractionClient(HttpConfig(token = token, logging = logging, httpClientConfig = httpClientConfig))

    fun httpClient(builder: HttpClientConfig<*>.() -> Unit) { httpClientConfig = builder }

}

/**
 * The HttpInteractionClient is used when you want to receive interactions over a post request rather than connecting to a websocket
 */
fun buildRestOnlyClient(token: String, builder: HttpInteractionClientBuilder.() -> Unit) =  HttpInteractionClientBuilder(token).apply(builder).build()
