package io.github.jan.discordkm.internal.restaction

import com.soywiz.korio.net.http.HttpClient

data class Request(val endpoint: String, val statement: suspend () -> HttpClient.Response) {

    suspend fun execute() = statement()

}