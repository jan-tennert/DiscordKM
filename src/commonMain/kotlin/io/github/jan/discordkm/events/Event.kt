package io.github.jan.discordkm.events

import io.github.jan.discordkm.clients.Client

sealed interface Event {

    val client: Client

}