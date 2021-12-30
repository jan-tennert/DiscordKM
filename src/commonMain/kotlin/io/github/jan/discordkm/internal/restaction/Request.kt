package io.github.jan.discordkm.internal.restaction

import io.ktor.client.statement.HttpStatement

data class Request(val endpoint: String, val statement: suspend () -> HttpStatement) {

    suspend fun execute() = statement().execute()

}