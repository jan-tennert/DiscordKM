package io.github.jan.discordkm.internal.restaction

import com.soywiz.klock.TimeSpan
import com.soywiz.klogger.Logger
import io.github.jan.discordkm.internal.exceptions.RestException
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.string
import io.github.jan.discordkm.internal.utils.toJsonObject
import io.github.jan.discordkm.internal.websocket.GatewayErrors
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.content.TextContent
import io.ktor.websocket.CloseReason

interface DiscordError {
    val code: Short
    val message: String
}

object ErrorHandler {

    suspend fun handle(response: HttpResponse) {
        if(response.status.value in 200..204) return
        val statusCode = response.status.value
        val data = response.body<String>().toJsonObject()
        val message = data["message"]?.string ?: "Unknown error"
        val errorCode = data["code"]?.int ?: 0
        val body = response.request.content as TextContent
        throw RestException(message, errorCode, statusCode, body.text, response.request.url.toString(), data.toString())
    }

    fun handle(error: CloseReason, logger: Logger, reconnect: TimeSpan) {
        val code = error.code
        val message = GatewayErrors.values().first { it.code == code }
        logger.error { "Disconnected due to an error: ${message.message} Code: $code. Trying to reconnect in $reconnect" }
    }


}



