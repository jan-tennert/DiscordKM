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

abstract class RestAction<T> internal constructor(private val action: Action, val client: Client) {

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
        fun get(endpoint: String) = Action(endpoint, HttpMethod.Get)
        fun post(endpoint: String, body: Any? = null) = Action(endpoint, HttpMethod.Post, body?.toString())
        fun delete(endpoint: String) = Action(endpoint, HttpMethod.Delete)
        fun patch(endpoint: String, body: Any ? = null) = Action(endpoint, HttpMethod.Patch, body?.toString())
        fun put(endpoint: String, body: Any? = null) = Action(endpoint, HttpMethod.Put, body?.toString())
    }

}

data class Action internal constructor(val endpoint: String, val method: HttpMethod, val body: String? = null)
typealias RestActionListener <T> = suspend (T) -> Unit

class RestActionBuilder<T>(val client: Client)  {

    lateinit var action: Action
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

    fun build() = object : RestAction<T>(action, client) {
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