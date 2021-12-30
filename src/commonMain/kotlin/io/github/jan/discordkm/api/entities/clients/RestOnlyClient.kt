/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.clients

import com.soywiz.klogger.Logger
import io.github.jan.discordkm.internal.caching.CacheFlag
import io.ktor.client.HttpClientConfig

/**
 * The RestOnlyClient is used when you only want to make REST API requests. The cache will be always empty.
 */
class RestOnlyClient @Deprecated("Use the method buildRestOnlyClient") internal constructor (config: ClientConfig) : Client(config) {

    override suspend fun disconnect() {
        requester.http.close()
    }

    override suspend fun login() {
    }

}

class RestOnlyClientBuilder(var token: String) {

    var loggingLevel = Logger.Level.DEBUG
    var enabledCache = CacheFlag.values().toMutableSet()
    private var httpClientConfig: HttpClientConfig<*>.() -> Unit = {}

    @Suppress("DEPRECATION")
    fun build() = RestOnlyClient(ClientConfig(token = token, loggingLevel = loggingLevel, enabledCache = enabledCache, httpClientConfig = httpClientConfig))

    fun httpClient(builder: HttpClientConfig<*>.() -> Unit) { httpClientConfig = builder }

}

/**
 * The RestOnlyClient is used when you only want to make REST API requests. The cache will be always empty.
 */
fun buildRestOnlyClient(token: String, builder: RestOnlyClientBuilder.() -> Unit) =  RestOnlyClientBuilder(token).apply(builder).build()
