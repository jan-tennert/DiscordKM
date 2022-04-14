/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.restaction

import com.soywiz.korio.net.http.Http
import com.soywiz.korio.stream.openAsync
import io.github.jan.discordkm.api.entities.clients.DiscordClient

typealias RestActionListener <T> = suspend (T) -> Unit

class FormattedRoute internal constructor(val endpoint: String, val method: Http.Method, val body: Any? = null)

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
        val http = requester.http
        check()
        val body = route.body?.let {
        //    if(it is Multipart) {
         //       it.stream
        //    } else {
                it.toString().openAsync()
        //    }
        }
        val request = Request(route.endpoint) {
            http.request(method = route.method, url = generateUrl(route.endpoint), headers = Http.Headers.build {
                put("X-Audit-Log-Reason", reason)
                put("Authorization", "Bot ${requester.config["token"]}")
                put("User-Agent", "Discord.KM (\$https://github.com/jan-tennert/Discord.KM, $0.3)")
                put(Http.Headers.ContentType, /*if(route.body is Multipart) "multipart/form-data; boundary=boundary" else*/ "application/json")
            }, content = body)
        }
        val response = requester.handle(request)
        val result = if (!this::transformer.isInitialized) Unit as T else transformer(response.readAllString())
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
        fun get(endpoint: String) = FormattedRoute(endpoint, Http.Methods.GET)
        fun post(endpoint: String, body: Any? = null) = FormattedRoute(endpoint, Http.Methods.POST, body)
        fun delete(endpoint: String) = FormattedRoute(endpoint, Http.Methods.DELETE)
        fun patch(endpoint: String, body: Any? = null) = FormattedRoute(endpoint, Http.Methods.PATCH, body)
        fun put(endpoint: String, body: Any? = null) = FormattedRoute(endpoint, Http.Methods.PUT, body)
    }

}

suspend inline fun <T> DiscordClient.buildRestAction(init: RestAction<T>.() -> Unit) = RestAction<T>(requester).apply(init).queue()