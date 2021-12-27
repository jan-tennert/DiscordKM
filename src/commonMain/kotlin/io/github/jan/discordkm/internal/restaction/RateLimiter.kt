/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.restaction

import co.touchlab.stately.collections.IsoMutableMap
import co.touchlab.stately.collections.IsoMutableSet
import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.seconds
import com.soywiz.klogger.Logger
import com.soywiz.korio.async.async
import com.soywiz.korio.async.delay
import io.github.jan.discordkm.internal.utils.LoggerOutput
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMessage
import io.ktor.util.StringValues
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

class RateLimiter(loggingLevel: Logger.Level) {

    private val buckets = IsoMutableMap<String, RestClient.Bucket>()
    private val tasks = IsoMutableSet<Deferred<HttpResponse>>()
    private val dispatcher = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            block.run()
        }
    }
    private val scope = CoroutineScope(dispatcher)
    private val LOGGER = Logger("RateLimiter")

    init {
        LOGGER.level = loggingLevel
        LOGGER.output = LoggerOutput
    }

    suspend fun queue(endpoint: String, task: suspend () -> HttpResponse): HttpResponse {
        while(tasks.isNotEmpty());
        val job = scope.async {
            if (endpoint in buckets) {
                val bucket = buckets[endpoint]!!
                if (bucket.remaining == 0) {
                    LOGGER.warn { "Ratelimit remaining is 0 \"$endpoint\". Request will be sent in: ${bucket.resetAfter.seconds} seconds" }
                    delay(bucket.resetAfter)
                    send(endpoint, task)
                } else {
                    send(endpoint, task)
                }
            } else {
                send(endpoint, task)
            }
        }
        tasks += job
        job.invokeOnCompletion {
            tasks -= job
        }
        return job.await()
    }

    private suspend fun send(endpoint: String, task: suspend () -> HttpResponse): HttpResponse {
        val response = task()
        val headers = (response as HttpMessage).headers as StringValues
        if(headers["x-ratelimit-bucket"] == null) return response
        val bucket = RestClient.Bucket(
            headers["x-ratelimit-bucket"]!!,
            headers["x-ratelimit-limit"]!!.toInt(),
            headers["x-ratelimit-remaining"]!!.toInt(),
            headers["x-ratelimit-reset-after"]!!.toDouble().seconds,
            DateTimeTz.fromUnixLocal(headers["x-ratelimit-reset"]!!.toDouble() * 1000)
        )
        buckets[endpoint] = bucket
        LOGGER.debug { "Received bucket ${bucket.bucket} on endpoint \"${endpoint}\". Remaining requests: ${bucket.remaining}" }
        return response
    }

}

typealias Task = suspend() -> HttpResponse
