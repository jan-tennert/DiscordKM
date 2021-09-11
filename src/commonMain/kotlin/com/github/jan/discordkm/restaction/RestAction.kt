package com.github.jan.discordkm.restaction

import com.github.jan.discordkm.Client
import io.ktor.http.HttpMethod
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

abstract class RestAction<T> internal constructor(private val action: Action, val client: Client) {

    private var listeners: MutableList<RestActionListener<T>> = mutableListOf()
    private var errorListener: RestActionErrorListener = { throw it }
    private var check: () -> Throwable? = {null}

    fun onError(listener: RestActionErrorListener): RestAction<T> { errorListener = listener; return this }

    fun onFinish(listener: RestActionListener<T>): RestAction<T> { this.listeners += listener; return this }

    @PublishedApi
    internal fun checkBeforeSending(check: () -> Throwable?) : RestAction<T> { this.check = check; return this }

    private fun send() {
        check()?.let {
            errorListener(it)
            return
        }
        client.launch {
            val result = client.rest.custom(action.method, action.endpoint, action.body)
            println(result)
            listeners.forEach { it((transform(result))) }
        }
    }

    suspend fun await() = suspendCancellableCoroutine<T> {
        onFinish { value ->
            it.resume(value) { error -> errorListener(error) }
        }
        send()
    }

    abstract fun transform(data: String) : T

    data class Action internal constructor(val endpoint: String, val method: HttpMethod, val body: String? = null) {

        companion object {

            fun get(endpoint: String) = Action(endpoint, HttpMethod.Get)
            fun post(endpoint: String, body: Any) = Action(endpoint, HttpMethod.Post, body.toString())
            fun delete(endpoint: String) = Action(endpoint, HttpMethod.Delete)
            fun patch(endpoint: String, body: Any) = Action(endpoint, HttpMethod.Patch, body.toString())
            fun custom(endpoint: String, method: HttpMethod, body: String? = null) = Action(endpoint, method, body)

        }

    }

}

typealias RestActionListener <T> = suspend (T) -> Unit
typealias RestActionErrorListener = (Throwable) -> Unit

class RestActionBuilder<T>(val client: Client)  {

    lateinit var action: RestAction.Action
    private lateinit var transform: (String) -> T
    @PublishedApi
    internal var check: Check = { null }
    @PublishedApi
    internal var onFinish: (T) -> Unit = {}

    fun transform(transform: (String) -> T) {
        this.transform = transform
    }

    fun onFinish(onFinish: (T) -> Unit) { this.onFinish = onFinish }

    fun check(check: Check) { this.check = check }

    fun build() = object : RestAction<T>(action, client) {
        override fun transform(data: String): T = this@RestActionBuilder.transform(data)
    }

}

typealias Check = () -> Throwable?

suspend inline fun <T> Client.buildRestAction(init: RestActionBuilder<T>.() -> Unit): T {
    val builder = RestActionBuilder<T>(this).apply(init)
    return builder.build()
        .checkBeforeSending(builder.check)
        .onFinish { builder.onFinish(it) }
        .await()
}