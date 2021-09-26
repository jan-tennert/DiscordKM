/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.restaction

import io.github.jan.discordkm.api.entities.clients.Client
import io.ktor.http.HttpMethod

abstract class RestAction<T> internal constructor(private val action: FormattedRoute, val client: Client) {

    @PublishedApi
    internal var check: () -> Unit = { }
    @PublishedApi
    internal var listener: RestActionListener<T> = {}

    @PublishedApi
    internal fun checkBeforeSending(check: () -> Unit) : RestAction<T> { this.check = check; return this }

    suspend fun await(): T {
        check()
        val json = client.rest.custom(action.method, action.endpoint, action.body)
        val result = transform(json)
        listener(result)
        return result
    }

    abstract fun transform(data: String) : T

    companion object {
        fun get(endpoint: String) = FormattedRoute(endpoint, HttpMethod.Get)
        fun post(endpoint: String, body: Any? = null) = FormattedRoute(endpoint, HttpMethod.Post, body)
        fun delete(endpoint: String) = FormattedRoute(endpoint, HttpMethod.Delete)
        fun patch(endpoint: String, body: Any ? = null) = FormattedRoute(endpoint, HttpMethod.Patch, body)
        fun put(endpoint: String, body: Any? = null) = FormattedRoute(endpoint, HttpMethod.Put, body)
    }

}

typealias RestActionListener <T> = suspend (T) -> Unit

class FormattedRoute internal constructor(val endpoint: String, val method: HttpMethod, val body: Any? = null)

class RestActionBuilder<T>(val client: Client)  {

    lateinit var route: FormattedRoute
    private lateinit var transform: (String) -> T
    @PublishedApi
    internal var check: () -> Unit = {  }
    @PublishedApi
    internal var onFinish: RestActionListener<T> = {}

    fun transform(transform: (String) -> T) {
        this.transform = transform
    }

    fun onFinish(onFinish: RestActionListener<T>) { this.onFinish = onFinish }

    fun check(check: () -> Unit) { this.check = check }

    fun build() = object : RestAction<T>(route, client) {
        override fun transform(data: String): T = this@RestActionBuilder.transform(data)
    }

}

suspend inline fun <T> Client.buildRestAction(init: RestActionBuilder<T>.() -> Unit): T {
    val builder = RestActionBuilder<T>(this).apply(init)
    return builder.build()
        .apply {
            listener = builder.onFinish
            check = builder.check
        }
        .await()
}