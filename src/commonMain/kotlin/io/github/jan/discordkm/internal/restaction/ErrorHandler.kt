/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.restaction

import com.soywiz.klock.TimeSpan
import com.soywiz.klogger.Logger
import com.soywiz.korio.net.http.HttpClient
import com.soywiz.korio.net.ws.WsCloseInfo

interface DiscordError {
    val code: Short
    val message: String
}

object ErrorHandler {

    suspend fun handle(response: HttpClient.Response) {
        /*if(response.status.value in 200..204) return
        val statusCode = response.status.value
        val data = response.receive<String>().toJsonObject()
        val message = data["message"]?.string ?: "Unknown error"
        val errorCode = data["code"]?.int ?: 0
        val body = response.request.content as TextContent
        throw RestException(message, errorCode, statusCode, body.text, response.request.url.toString(), data.toString())*/
    }

    fun handle(error: WsCloseInfo, logger: Logger, reconnect: TimeSpan) {
        /*val code = error.code
        val message = GatewayErrors.values().firstOrNull { it.code == code } ?: return
        logger.error { "Disconnected due to an error: ${message.message} Code: $code. Trying to reconnect in $reconnect" }*/
    }


}



