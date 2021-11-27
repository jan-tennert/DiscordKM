package io.github.jan.discordkm.internal.restaction

import com.soywiz.klogger.Logger
import com.soywiz.korio.net.http.Http
import com.soywiz.korio.net.http.createHttpClient
import com.soywiz.korio.stream.openAsync
import com.soywiz.korio.stream.readStringz

actual abstract class RestHandler {

    actual abstract val rateLimiter: RateLimiter

    actual abstract val loggingLevel: Logger.Level

    actual abstract val token: String?

    private val http = createHttpClient()

    val defaultHeaders = Http.Headers.build {
        put("Authorization", "Bot $token")
        put("User-Agent", "Discord.KM (\$https://github.com/jan-tennert/Discord.KM, $0.3)")
    }

    actual suspend fun custom(
        method: HttpMethod,
        endpoint: String,
        requestBody: Any?,
        reason: String?
    ) = when (method) {
        HttpMethod.GET -> {
            rateLimiter.queue(endpoint) {
                val request = http.request(
                    Http.Method.GET,
                    generateUrl(endpoint),
                    headers = defaultHeaders.apply { if (reason != null) withAppendedHeaders("X-Audit-Log-Reason" to reason) }
                )
                val body = request.content.readStringz()
                Response(body, request.headers.toMap())
            }
        }
        HttpMethod.POST -> {
            rateLimiter.queue(endpoint) {
                val request = http.request(
                    Http.Method.POST,
                    generateUrl(endpoint),
                    content = requestBody.toString().openAsync(), //multipart
                    headers = defaultHeaders.apply { if (reason != null) withAppendedHeaders("X-Audit-Log-Reason" to reason) }
                )
                val body = request.content.readStringz()
                Response(body, request.headers.toMap())
            }
        }
        HttpMethod.DELETE -> {
            rateLimiter.queue(endpoint) {
                val request = http.request(
                    Http.Method.POST,
                    generateUrl(endpoint),
                    content = requestBody.toString().openAsync(), //multipart
                    headers = defaultHeaders.apply { if (reason != null) withAppendedHeaders("X-Audit-Log-Reason" to reason) }
                )
                val body = request.content.readStringz()
                Response(body, request.headers.toMap())
            }
        }
        HttpMethod.PATCH -> {
            rateLimiter.queue(endpoint) {
                val request = http.request(
                    Http.Method.PATCH,
                    generateUrl(endpoint),
                    content = requestBody.toString().openAsync(), //multipart
                    headers = defaultHeaders.apply { if (reason != null) withAppendedHeaders("X-Audit-Log-Reason" to reason) }
                )
                val body = request.content.readStringz()
                Response(body, request.headers.toMap())
            }

        }
        HttpMethod.PUT -> {
            rateLimiter.queue(endpoint) {
                val request = http.request(
                    Http.Method.PUT,
                    generateUrl(endpoint),
                    content = requestBody.toString().openAsync(), //multipart
                    headers = defaultHeaders.apply { if (reason != null) withAppendedHeaders("X-Audit-Log-Reason" to reason) }
                )
                val body = request.content.readStringz()
                Response(body, request.headers.toMap())
            }

        }
    }

}