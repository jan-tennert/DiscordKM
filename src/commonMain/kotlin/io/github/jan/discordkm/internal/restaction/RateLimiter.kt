/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.restaction

import co.touchlab.stately.collections.IsoMutableList
import co.touchlab.stately.collections.IsoMutableMap
import com.soywiz.klock.DateTime
import com.soywiz.klock.seconds
import com.soywiz.klogger.Logger
import com.soywiz.korio.async.delay
import com.soywiz.korio.async.launch
import io.github.jan.discordkm.internal.utils.LoggerOutput
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMessage
import io.ktor.util.StringValues
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RateLimiter(loggingLevel: Logger.Level) {

    private val buckets = IsoMutableMap<String, RestClient.Bucket>()
    private val tasks = IsoMutableMap<String, IsoMutableList<Task>>()
    private val scope = GlobalScope
    private val LOGGER = Logger("RateLimiter")
    private var open = true
    val mutex = Mutex()

    init {
        LOGGER.level = loggingLevel
        LOGGER.output = LoggerOutput
    }

    suspend fun queue(endpoint: String, task: suspend () -> HttpResponse): HttpResponse {
        while(tasks[endpoint]?.isNotEmpty() == true);
        return waitForResponse(endpoint, task)
    }

    private suspend fun waitForResponse(endpoint: String, task: suspend () -> HttpResponse) = suspendCancellableCoroutine<HttpResponse> {
        if(endpoint in tasks) {
            tasks[endpoint]!!.add(task to it)
        } else {
            val list = IsoMutableList<Task>()
            list.add(task to it)
            tasks[endpoint] = list
        }
    }

    suspend fun startRequester() {
        while(open) {
            val tasksCopy = tasks.access { it.toList() }
            for (task in tasksCopy) {
                coroutineScope {
                    launch {
                        val (endpoint, tasks) = task
                        val endpointTasks = tasks.access { it.toList() }
                        for (endpointTask in endpointTasks) {
                            runWithBucket(endpoint, endpointTask)
                        }
                    }
                }
            }
        }
    }

    suspend fun stopRequester() {
        mutex.withLock {
            open = false
        }
    }

    private suspend fun runWithBucket(endpoint: String, task: Task) {
        val bucket = buckets[endpoint]
        val (response, continuation) = task
        if(bucket == null) {
            continuation.resume(send(endpoint, response)) {
                it.printStackTrace()
            }
        } else {
            if(bucket.remaining == 0) {
                LOGGER.warn { "Remaining requests on $endpoint are used up. Waiting ${bucket.resetAfter}" }
                delay(bucket.resetAfter)
            }
            continuation.resume(send(endpoint, response)) {
                it.printStackTrace()
            }
        }
        tasks[endpoint]!!.remove(task)
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
            DateTime.fromUnix(headers["x-ratelimit-reset"]!!.toDouble() * 1000)
        )
        buckets[endpoint] = bucket
        LOGGER.debug { "Received bucket ${bucket.bucket} on endpoint \"${endpoint}\". Remaining requests: ${bucket.remaining}" }
        return response
    }

}

typealias Task = Pair<suspend () -> HttpResponse, CancellableContinuation<HttpResponse>>
