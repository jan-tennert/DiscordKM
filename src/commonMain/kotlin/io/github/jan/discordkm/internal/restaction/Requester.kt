package io.github.jan.discordkm.internal.restaction

import io.github.jan.discordkm.DiscordKMInfo
import io.github.jan.discordkm.api.entities.clients.ClientConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse

class Requester(private val config: ClientConfig) {

    private val rateLimiter = RateLimiter(config.loggingLevel)
    private val errorHandler = ErrorHandler(config)

    val http = HttpClient {
        config.httpClientConfig(this)
        defaultRequest {
            header("Authorization", "Bot ${config.token}")
            header("User-Agent", "Discord.KM (\$https://github.com/jan-tennert/Discord.KM, $0.3)")
        }

        expectSuccess = false
    }

    suspend fun handle(request: Request) : HttpResponse {
        val response = rateLimiter.queue(request)
        rateLimiter.updateRateLimits(request, response)
        errorHandler.handle(response)
        return response
    }

}

fun generateUrl(endpoint: String) = "https://discord.com/api/v${DiscordKMInfo.DISCORD_API_VERSION}$endpoint"