package io.github.jan.discordkm.internal.restaction

import com.soywiz.klogger.Logger
import io.github.jan.discordkm.api.entities.clients.ClientConfig
import io.github.jan.discordkm.internal.exceptions.RestException
import io.github.jan.discordkm.internal.utils.LoggerOutput
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.string
import io.github.jan.discordkm.internal.utils.toJsonObject
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.statement.request
import io.ktor.http.content.TextContent

class ErrorHandler(config: ClientConfig) {

    private val LOGGER = Logger("ErrorHandler")

    init {
        LOGGER.level = config.loggingLevel
        LOGGER.output = LoggerOutput
    }

    suspend fun handle(error: ClientRequestException) {
        val response = error.response
        if(response.status.value in 200..201) return
        val statusCode = response.status.value
        val data = response.body<String>().toJsonObject()
        val message = data["message"]?.string ?: "Unknown error"
        val errorCode = data["code"]?.int ?: 0
        val body = response.request.content as TextContent
        LOGGER.error { RestException(message, errorCode, statusCode, body.text, response.request.url.toString()).message }
    }

}