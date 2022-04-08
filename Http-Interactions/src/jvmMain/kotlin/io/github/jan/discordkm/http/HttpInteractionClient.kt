/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.http

import io.github.jan.discordkm.api.entities.UserCacheEntry
import io.github.jan.discordkm.api.entities.clients.ClientConfig
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.containers.CacheChannelContainer
import io.github.jan.discordkm.api.entities.containers.CacheGuildContainer
import io.github.jan.discordkm.api.entities.containers.CacheMemberContainer
import io.github.jan.discordkm.api.entities.containers.CacheThreadContainer
import io.github.jan.discordkm.api.entities.containers.CacheUserContainer
import io.github.jan.discordkm.internal.DiscordKMInternal
import io.github.jan.discordkm.internal.restaction.Requester
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


class HttpInteractionClient internal constructor(override val config: ClientConfig) : DiscordClient {

    private lateinit var server: ApplicationEngine
    private val LOGGER = config.map<LoggerConfig>("logging")("Http-Server")
    override var selfUser: UserCacheEntry = throw IllegalStateException("HttpInteractionClient does not support selfUser")
    override val guilds = CacheGuildContainer(this, emptyList())
    override val users = CacheUserContainer(this, emptyList())
    override val channels = CacheChannelContainer(emptyList())
    override val members = CacheMemberContainer(emptyList())
    override val threads = CacheThreadContainer(emptyList())
    override val requester = Requester(config)

    override suspend fun disconnect() {
        server.stop(1000, 1000)
    }

    override suspend fun login() {
        server = embeddedServer(CIO, port = config.map<Int>("port"), host = config.map<String>("host")) {
            routing {
                post(config.map<String>("route")) {
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

    fun build() = HttpInteractionClient(ClientConfig(mapOf(
        "token" to token,
        "logging" to logging,
        "httpClientConfig" to httpClientConfig,
        "port" to port,
        "host" to host,
        "route" to route
    )))

    fun httpClient(builder: HttpClientConfig<*>.() -> Unit) { httpClientConfig = builder }

}

/*
 * The HttpInteractionClient is used when you want to receive interactions over a post request rather than connecting to a websocket
 */
@OptIn(DiscordKMInternal::class)
inline fun buildHttpInteractionClient(token: String, builder: HttpInteractionClientBuilder.() -> Unit) =  HttpInteractionClientBuilder(token).apply(builder).build()
