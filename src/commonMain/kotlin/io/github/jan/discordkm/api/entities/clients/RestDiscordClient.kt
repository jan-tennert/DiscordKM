/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.clients

import com.soywiz.klogger.Logger
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.containers.CacheChannelContainer
import io.github.jan.discordkm.api.entities.containers.CacheGuildContainer
import io.github.jan.discordkm.api.entities.containers.CacheMemberContainer
import io.github.jan.discordkm.api.entities.containers.CacheThreadContainer
import io.github.jan.discordkm.api.entities.containers.CacheUserContainer
import io.github.jan.discordkm.api.entities.misc.TranslationManager
import io.github.jan.discordkm.internal.DiscordKMInternal
import io.github.jan.discordkm.internal.caching.CacheFlag
import io.github.jan.discordkm.internal.restaction.Requester
import io.github.jan.discordkm.internal.utils.LoggerConfig
import io.github.jan.discordkm.internal.utils.log
import io.ktor.client.HttpClientConfig

/*
 * The RestOnlyClient is used when you only want to make REST API requests. The cache will be always empty.
 */
class RestDiscordClient internal constructor(override val config: ClientConfig, botId: Snowflake) : DiscordClient {

    override val requester = Requester(config)
    override var selfUser = User(botId, this)
    override val guilds = CacheGuildContainer(this, emptyList())
    override val users = CacheUserContainer(this, emptyList())
    override val members = CacheMemberContainer(emptyList())
    override val channels = CacheChannelContainer(emptyList())
    override val threads = CacheThreadContainer(emptyList())
    private val LOGGER = config.map<LoggerConfig>("logging")("DiscordKM")

    init {
        LOGGER.log(true, Logger.Level.WARN) {
            "Warning: This is a beta version of DiscordKM, please report any bugs you find!"
        }
    }

    override suspend fun disconnect() {
        requester.http.close()
    }

    override suspend fun login() {

    }

}

class RestOnlyClientBuilder @DiscordKMInternal constructor(var token: String, var botId: Snowflake) {

    var logging = LoggerConfig()
    var enabledCache = CacheFlag.values().toMutableSet()
    var translationManager = TranslationManager.empty()
    private var httpClientConfig: HttpClientConfig<*>.() -> Unit = {}

    fun build(): RestDiscordClient = RestDiscordClient(
        ClientConfig(
            mapOf(
                "token" to token,
                "logging" to logging,
                "enabledCache" to enabledCache,
                "translationManager" to translationManager,
                "httpClientConfig" to httpClientConfig
            )
        ), botId
    )

    fun httpClient(builder: HttpClientConfig<*>.() -> Unit) { httpClientConfig = builder }

}

/*
 * The RestOnlyClient is used when you only want to make REST API requests. The cache will be always empty.
 */
@OptIn(DiscordKMInternal::class)
inline fun buildRestClient(token: String, botId: Snowflake, builder: RestOnlyClientBuilder.() -> Unit) =  RestOnlyClientBuilder(token, botId).apply(builder).build()
