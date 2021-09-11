package io.github.jan.discordkm.events.internal

import io.github.jan.discordkm.events.ReadyEvent
import kotlinx.serialization.json.JsonObject

internal interface InternalEventHandler <T> {

    fun handle(data: JsonObject) : T

}