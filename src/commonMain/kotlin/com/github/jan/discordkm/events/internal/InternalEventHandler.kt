package com.github.jan.discordkm.events.internal

import com.github.jan.discordkm.events.ReadyEvent
import kotlinx.serialization.json.JsonObject

internal interface InternalEventHandler <T> {

    fun handle(data: JsonObject) : T

}