package io.github.jan.discordkm.restaction

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.utils.DiscordKMInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod

class RestClient(val client: Client) {

    private val http = HttpClient {
        defaultRequest {
            header("Authorization", "Bot ${client.token}")
            header("User-Agent", "Discord.KM (\$https://github.com/jan-tennert/Discord.KM, 0.1)")
        }
    }
    internal val rateLimiter = RateLimiter(client.loggingLevel)

    suspend fun get(endpoint: String) = custom(HttpMethod.Get, endpoint)
    suspend fun post(endpoint: String, data: String) = custom(HttpMethod.Post, endpoint, data)
    suspend fun delete(endpoint: String) = custom(HttpMethod.Delete, endpoint)
    suspend fun patch(endpoint: String, data: String) = custom(HttpMethod.Patch, endpoint, data)
    suspend fun custom(method: HttpMethod, endpoint: String, data: String? = null) = when(method.value) {
        "GET" -> {
            rateLimiter.queue(endpoint) {
                http.get<HttpResponse>(generateUrl(endpoint))
            }.receive<String>()
        }
        "POST" -> {
            rateLimiter.queue(endpoint) {
                http.post<HttpResponse>(generateUrl(endpoint)) {
                    body = data!!

                    header("Content-Type", "application/json")
                }
            }.receive<String>()
        }
        "DELETE" -> {
            rateLimiter.queue(endpoint) {
                http.delete<HttpResponse>(generateUrl(endpoint))
            }.receive<String>()
        }
        "PATCH" -> {
            println(data!!)
            rateLimiter.queue(endpoint) {
                http.patch<HttpResponse>(generateUrl(endpoint)) {
                    body = data!!

                    header("Content-Type", "application/json")
                }
            }.receive<String>()
        }
        else -> throw UnsupportedOperationException()
    }

    data class Bucket(val bucket: String, val limit: Int, val remaining: Int, val resetAfter: TimeSpan, val reset: DateTimeTz)

}

fun generateUrl(endpoint: String) = "https://discord.com/api/v${DiscordKMInfo.DISCORD_API_VERSION}$endpoint"