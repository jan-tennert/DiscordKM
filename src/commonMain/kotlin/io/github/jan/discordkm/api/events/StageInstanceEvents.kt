package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.StageInstance

interface StageInstanceEvent : Event {

    val stageInstance: StageInstance
    override val client: Client
        get() = stageInstance.client

}

class StageInstanceCreateEvent(override val stageInstance: StageInstance) : StageInstanceEvent
class StageInstanceDeleteEvent(override val stageInstance: StageInstance) : StageInstanceEvent
class StageInstanceUpdateEvent(override val stageInstance: StageInstance, val oldStageInstance: StageInstance?) : StageInstanceEvent