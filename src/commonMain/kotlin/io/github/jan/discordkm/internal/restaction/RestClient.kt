/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.restaction

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.DiscordKMInfo
import io.github.jan.discordkm.api.entities.clients.ClientConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType

class RestClient(private val config: ClientConfig) {

    private val errorHandler = ErrorHandler(config)

    val http = HttpClient {
        config.httpClientConfig(this)
        defaultRequest {
            header("Authorization", "Bot ${config.token}")
            header("User-Agent", "Discord.KM (\$https://github.com/jan-tennert/Discord.KM, $0.3)")
        }


        expectSuccess = false
    }
    val rateLimiter = RateLimiter(config.loggingLevel)

    suspend fun custom(method: HttpMethod, endpoint: String, data: Any? = null, reason: String? = null) =
        when(method.value) {
            "GET" -> {
                rateLimiter.queue(endpoint) {
                    http.get(generateUrl(endpoint)) {
                        header("X-Audit-Log-Reason", reason)
                    }
                }
            }
            "POST" -> {
                rateLimiter.queue(endpoint) {
                    http.post(generateUrl(endpoint)) {
                        header("X-Audit-Log-Reason", reason)
                        data?.let {
                            setBody(
                                if (it is MultiPartFormDataContent) {
                                    it
                                } else {
                                    contentType(ContentType.Application.Json)
                                    it.toString()
                                }
                            )
                        }
                    }
                }
            }
            "DELETE" -> {
                rateLimiter.queue(endpoint) {
                    http.delete(generateUrl(endpoint)) {
                        header("X-Audit-Log-Reason", reason)
                    }
                }
            }
            "PATCH" -> {
                rateLimiter.queue(endpoint) {
                    http.patch(generateUrl(endpoint)) {
                        header("X-Audit-Log-Reason", reason)
                        data?.let {
                            setBody(if(it is MultiPartFormDataContent) {
                                it
                            } else {
                                contentType(ContentType.Application.Json)
                                it.toString()
                            })
                        }
                    }
                }
            }
            "PUT" -> {
                rateLimiter.queue(endpoint) {
                    http.put(generateUrl(endpoint)) {
                        header("X-Audit-Log-Reason", reason)
                        data?.let {
                            setBody(if(it is MultiPartFormDataContent) {
                                it
                            } else {
                                contentType(ContentType.Application.Json)
                                it.toString()
                            })
                        }
                    }
                }
            }
            else -> throw UnsupportedOperationException()
        }.let {
            if(it.status.value in 200..204) {
                it.body<String>()
            } else {
                errorHandler.handle(it)
            }
        }


    data class Bucket(val bucket: String, val limit: Int, val remaining: Int, val resetAfter: TimeSpan, val reset: DateTime)

}

fun generateUrl(endpoint: String) = "https://discord.com/api/v${DiscordKMInfo.DISCORD_API_VERSION}$endpoint"