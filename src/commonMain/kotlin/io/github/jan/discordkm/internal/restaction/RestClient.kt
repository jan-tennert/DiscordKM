/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.restaction

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.internal.utils.DiscordKMInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod

class RestClient(val client: Client) {

    private val http = HttpClient {
        defaultRequest {
            header("Authorization", "Bot ${client.token}")
            header("User-Agent", "Discord.KM (\$https://github.com/jan-tennert/Discord.KM, 0.1)")
        }
    }
    internal val rateLimiter = RateLimiter(client.loggingLevel)

    suspend fun custom(method: HttpMethod, endpoint: String, data: Any? = null) = when(method.value) {
        "GET" -> {
            rateLimiter.queue(endpoint) {
                http.get<HttpResponse>(generateUrl(endpoint))
            }.receive<String>()
        }
        "POST" -> {
            println(data)
            rateLimiter.queue(endpoint) {
                http.post<HttpResponse>(generateUrl(endpoint)) {
                    data?.let {
                        body = it
                    }
                    header("Content-Type", "application/json")
                }
            }.receive<String>()
        }
        "DELETE" -> {
            rateLimiter.queue(endpoint) {
                http.delete<HttpResponse>(generateUrl(endpoint))
            }.receive<String>()
        }
        "PATCH" -> {
            rateLimiter.queue(endpoint) {
                http.patch<HttpResponse>(generateUrl(endpoint)) {
                    data?.let {
                        body = it
                    }
                    header("Content-Type", "application/json")
                }
            }.receive<String>()
        }
        "PUT" -> {
            rateLimiter.queue(endpoint) {
                http.put(generateUrl(endpoint)) {
                    data?.let {
                        body = it
                    }
                }
            }.receive<String>()
        }
        else -> throw UnsupportedOperationException()
    }

    data class Bucket(val bucket: String, val limit: Int, val remaining: Int, val resetAfter: TimeSpan, val reset: DateTimeTz)

}

fun generateUrl(endpoint: String) = "https://discord.com/api/v${DiscordKMInfo.DISCORD_API_VERSION}$endpoint"