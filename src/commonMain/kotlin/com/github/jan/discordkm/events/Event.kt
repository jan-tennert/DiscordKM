package com.github.jan.discordkm.events

import com.github.jan.discordkm.Client

sealed interface Event {

    val client: Client

}