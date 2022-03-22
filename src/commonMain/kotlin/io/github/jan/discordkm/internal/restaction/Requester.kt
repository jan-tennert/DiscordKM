package io.github.jan.discordkm.internal.restaction

import com.soywiz.korio.net.http.HttpClient
import com.soywiz.korio.net.http.createHttpClient
import io.github.jan.discordkm.DiscordKMInfo
import io.github.jan.discordkm.api.entities.clients.ClientConfig

class Requester(val config: ClientConfig) {

    private val rateLimiter = RateLimiter(config.logging)

    /*val http = HttpClient {
        config.httpClientConfig(this)
        defaultRequest {
            header("Authorization", "Bot ${config.token}")
            header("User-Agent", "Discord.KM (\$https://github.com/jan-tennert/Discord.KM, $0.3)")
        }

        expectSuccess = false
    }*/
    val http = createHttpClient()

    suspend fun handle(request: Request) : HttpClient.Response {
        val response = rateLimiter.queue(request)
        rateLimiter.updateRateLimits(request, response)
        ErrorHandler.handle(response)
        return response
    }

}

fun generateUrl(endpoint: String) = "https://discord.com/api/v${DiscordKMInfo.DISCORD_API_VERSION}$endpoint"