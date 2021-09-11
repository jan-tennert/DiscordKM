package com.github.jan.discordkm.events

interface EventListener {

    suspend fun onEvent(event: Event)

    suspend operator fun invoke(event: Event) = onEvent(event)

}

fun EventListener(onEvent: suspend (Event) -> Unit) = object : EventListener {
    override suspend fun onEvent(event: Event) = onEvent(event)
}