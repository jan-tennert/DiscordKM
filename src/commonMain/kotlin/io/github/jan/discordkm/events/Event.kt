package io.github.jan.discordkm.events

import io.github.jan.discordkm.Client

sealed interface Event {

    val client: Client

}