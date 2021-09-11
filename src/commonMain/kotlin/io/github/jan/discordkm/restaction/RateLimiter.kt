package io.github.jan.discordkm.restaction

import co.touchlab.stately.collections.IsoMutableMap
import co.touchlab.stately.collections.IsoMutableSet
import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.seconds
import com.soywiz.klogger.Logger
import com.soywiz.korio.async.delay
import io.github.jan.discordkm.utils.LoggerOutput
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMessage
import io.ktor.util.StringValues

class RateLimiter(loggingLevel: Logger.Level) {

    val buckets = IsoMutableMap<String, RestClient.Bucket>()
    val tasks = IsoMutableSet<Task>()

    private val LOGGER = Logger("RateLimiter")

    init {
        LOGGER.level = loggingLevel
        LOGGER.output = LoggerOutput
    }

    suspend fun queue(endpoint: String, task: suspend () -> HttpResponse): HttpResponse {
        return if (endpoint in buckets) {
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


    private suspend fun send(endpoint: String, task: suspend () -> HttpResponse): HttpResponse {
        val response = task()
        val headers = (response as HttpMessage).headers as StringValues
        if(headers["x-ratelimit-bucket"] == null) return response
        val bucket = RestClient.Bucket(
            headers["x-ratelimit-bucket"]!!,
            headers["x-ratelimit-limit"]!!.toInt(),
            headers["x-ratelimit-remaining"]!!.toInt(),
            headers["x-ratelimit-reset-after"]!!.toDouble().seconds,
            DateTimeTz.Companion.fromUnixLocal(headers["x-ratelimit-reset"]!!.toDouble() * 1000)
        )
        buckets[endpoint] = bucket
        LOGGER.debug { "Received bucket ${bucket.bucket} on endpoint \"${endpoint}\". Remaining requests: ${bucket.remaining}" }
        return response
    }

}

typealias Task = suspend() -> HttpResponse
