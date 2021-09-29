package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.guild.StageInstance
import io.github.jan.discordkm.api.events.StageInstanceCreateEvent
import io.github.jan.discordkm.api.events.StageInstanceDeleteEvent
import io.github.jan.discordkm.api.events.StageInstanceUpdateEvent
import io.github.jan.discordkm.internal.Cache
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import kotlinx.serialization.json.JsonObject

class StageInstanceCreateEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<StageInstanceCreateEvent> {

    override fun handle(data: JsonObject): StageInstanceCreateEvent {
        val stageInstance = StageInstance(client, data)
        if(Cache.STAGE_INSTANCES in client.enabledCache) (stageInstance.guild as GuildData).stageInstanceCache[stageInstance.id] = stageInstance
        return StageInstanceCreateEvent(stageInstance)
    }

}

class StageInstanceUpdateEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<StageInstanceUpdateEvent> {

    override fun handle(data: JsonObject): StageInstanceUpdateEvent {
        val stageInstance = StageInstance(client, data)
        val oldStageInstance = stageInstance.guild.stageInstances.firstOrNull { it.id == stageInstance.id }
        if(Cache.STAGE_INSTANCES in client.enabledCache) (stageInstance.guild as GuildData).stageInstanceCache[stageInstance.id] = stageInstance
        return StageInstanceUpdateEvent(stageInstance, oldStageInstance)
    }

}

class StageInstanceDeleteEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<StageInstanceDeleteEvent> {

    override fun handle(data: JsonObject): StageInstanceDeleteEvent {
        val stageInstance = StageInstance(client, data)
        if(Cache.STAGE_INSTANCES in client.enabledCache) (stageInstance.guild as GuildData).stageInstanceCache.remove(stageInstance.id)
        return StageInstanceDeleteEvent(stageInstance)
    }

}