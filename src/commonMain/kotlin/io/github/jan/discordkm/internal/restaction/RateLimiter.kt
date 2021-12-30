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
import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import com.soywiz.klogger.Logger
import com.soywiz.korio.async.async
import com.soywiz.korio.async.delay
import io.github.jan.discordkm.internal.utils.LoggerOutput
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Job
import kotlin.coroutines.coroutineContext

class RateLimiter(loggingLevel: Logger.Level) {

    private val buckets = IsoMutableMap<String, Bucket>()
    private val LOGGER = Logger("RateLimiter")
    private val jobs = IsoMutableSet<Job>()

    init {
        LOGGER.level = loggingLevel
        LOGGER.output = LoggerOutput
    }

    suspend fun queue(request: Request): HttpResponse {
        while(jobs.isNotEmpty())
        if(buckets[request.endpoint] != null && buckets[request.endpoint]!!.remaining == 0) {
            delay(buckets[request.endpoint]!!.resetAfter)
            return queue(request)
        }
        val job = async(coroutineContext) { request.execute() }
        jobs.add(job)
        val result = job.await()
        jobs -= job
        return result
    }

    fun updateRateLimits(request: Request, response: HttpResponse) {
        val headers = response.headers
        if("X-ratelimit-bucket" !in headers) return
        val bucket = Bucket(
            headers["x-ratelimit-bucket"]!!,
            headers["x-ratelimit-limit"]!!.toInt(),
            headers["x-ratelimit-remaining"]!!.toInt(),
            headers["x-ratelimit-reset-after"]!!.toDouble().seconds,
            DateTime.fromUnix(headers["x-ratelimit-reset"]!!.toDouble() * 1000)
        )
        buckets[request.endpoint] = bucket
    }

}

data class Bucket(val bucket: String, val limit: Int, val remaining: Int, val resetAfter: TimeSpan, val reset: DateTime)
