/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.restaction

import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.ktor.client.call.receive
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType

typealias RestActionListener <T> = suspend (T) -> Unit

class FormattedRoute internal constructor(val endpoint: String, val method: HttpMethod, val body: Any? = null)

class RestAction<T>(val requester: Requester) {

    lateinit var route: FormattedRoute
    private lateinit var transformer: (String) -> T
    private var check: () -> Unit = { }
    private var onFinish: RestActionListener<T> = {}
    var reason: String? = null

    fun transform(transform: (String) -> T) {
        this.transformer = transform
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun queue(): T {
        println(route.body)
        val http = requester.http
        check()
        val request = Request(route.endpoint) {
            when (route.method.value) {
                "GET" -> {
                    http.get(generateUrl(route.endpoint)) {
                        header("X-Audit-Log-Reason", reason)
                    }
                }
                "POST" -> {
                    http.post(generateUrl(route.endpoint)) {
                        header("X-Audit-Log-Reason", reason)
                        route.body.let {
                            body = if (it is MultiPartFormDataContent) {
                                it
                            } else {
                                contentType(ContentType.Application.Json)
                                it.toString()
                            }
                        }
                    }

                }
                "DELETE" -> {
                    http.delete(generateUrl(route.endpoint)) {
                        header("X-Audit-Log-Reason", reason)
                    }

                }
                "PATCH" -> {
                    http.patch(generateUrl(route.endpoint)) {
                        header("X-Audit-Log-Reason", reason)
                        route.body?.let {
                            body = if (it is MultiPartFormDataContent) {
                                it
                            } else {
                                contentType(ContentType.Application.Json)
                                it.toString()
                            }
                        }

                    }
                }
                "PUT" -> {
                    http.put(generateUrl(route.endpoint)) {
                        header("X-Audit-Log-Reason", reason)
                        route.body?.let {
                            body = if (it is MultiPartFormDataContent) {
                                it
                            } else {
                                contentType(ContentType.Application.Json)
                                it.toString()
                            }
                        }
                    }

                }
                else -> throw UnsupportedOperationException()
            }
        }
        val response = requester.handle(request)
        val result = if (!this::transformer.isInitialized) Unit as T else transformer(response.receive())
        onFinish(result)
        return result
    }

    fun onFinish(onFinish: RestActionListener<T>) {
        this.onFinish = onFinish
    }

    fun check(check: () -> Unit) {
        this.check = check
    }

    companion object {
        fun get(endpoint: String) = FormattedRoute(endpoint, HttpMethod.Get)
        fun post(endpoint: String, body: Any? = null) = FormattedRoute(endpoint, HttpMethod.Post, body)
        fun delete(endpoint: String) = FormattedRoute(endpoint, HttpMethod.Delete)
        fun patch(endpoint: String, body: Any? = null) = FormattedRoute(endpoint, HttpMethod.Patch, body)
        fun put(endpoint: String, body: Any? = null) = FormattedRoute(endpoint, HttpMethod.Put, body)
    }

}

suspend inline fun <T> DiscordClient.buildRestAction(init: RestAction<T>.() -> Unit) = RestAction<T>(requester).apply(init).queue()