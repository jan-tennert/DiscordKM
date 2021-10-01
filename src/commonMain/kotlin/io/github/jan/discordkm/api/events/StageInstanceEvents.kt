package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.StageInstance

interface StageInstanceEvent : Event {

    val stageInstance: StageInstance
    override val client: Client
        get() = stageInstance.client

}

/**
 * Sent when a stage instance was created
 */
class StageInstanceCreateEvent(override val stageInstance: StageInstance) : StageInstanceEvent

/**
 * Sent when a stage instance was updated
 */
class StageInstanceDeleteEvent(override val stageInstance: StageInstance) : StageInstanceEvent

/**
 * Sent when a stage instance was deleted
 */
class StageInstanceUpdateEvent(override val stageInstance: StageInstance, val oldStageInstance: StageInstance?) : StageInstanceEvent