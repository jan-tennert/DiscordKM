/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.restaction

import com.soywiz.korio.net.http.HttpClient
import com.soywiz.korio.net.http.createHttpClient
import io.github.jan.discordkm.DiscordKMInfo
import io.github.jan.discordkm.api.entities.clients.ClientConfig

class Requester(val config: ClientConfig) {

    private val rateLimiter = RateLimiter(config.map("logging"))

    /*val http = HttpClient {
        config.map<HttpClientConfig<*>.() -> Unit>("httpClientConfig")
        defaultRequest {
            header("Authorization", "Bot ${config.map<String>("token")}")
            header("User-Agent", "Discord.KM (\$https://github.com/jan-tennert/Discord.KM, $0.3)")
        }

        expectSuccess = false
    }*/
    val http = createHttpClient()

    suspend fun handle(request: Request) : HttpClient.Response {
        val response = rateLimiter.queue(request)
        rateLimiter.updateRateLimits(request, response)
        ErrorHandler.handle(response)
        return response
    }

}

fun generateUrl(endpoint: String) = "https://discord.com/api/v${DiscordKMInfo.DISCORD_API_VERSION}$endpoint"