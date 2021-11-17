package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEvent
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEvent.EventStatus.*
import io.github.jan.discordkm.api.events.ScheduledEventCancelEvent
import io.github.jan.discordkm.api.events.ScheduledEventCompleteEvent
import io.github.jan.discordkm.api.events.ScheduledEventCreate
import io.github.jan.discordkm.api.events.ScheduledEventDelete
import io.github.jan.discordkm.api.events.ScheduledEventStartEvent
import io.github.jan.discordkm.api.events.ScheduledEventUpdate
import io.github.jan.discordkm.internal.serialization.serializers.ScheduledEventSerializer
import kotlinx.serialization.json.JsonObject

class ScheduledEventCreateHandler(val client: Client) : InternalEventHandler<ScheduledEventCreate> {

    override suspend fun handle(data: JsonObject): ScheduledEventCreate {
        val scheduledEvent = ScheduledEventSerializer.deserialize(data, client)
        scheduledEvent.guild.cache?.cacheManager?.guildScheduledEventCache?.set(scheduledEvent.id, scheduledEvent)
        return ScheduledEventCreate(scheduledEvent)
    }

}

class ScheduledEventUpdateHandler(val client: Client) : InternalEventHandler<ScheduledEventUpdate> {

    override suspend fun handle(data: JsonObject): ScheduledEventUpdate {
        val scheduledEvent = ScheduledEventSerializer.deserialize(data, client)
        val oldScheduledEvent = scheduledEvent.guild.cache?.scheduledEvents?.get(scheduledEvent.id)
        scheduledEvent.guild.cache?.cacheManager?.guildScheduledEventCache?.set(scheduledEvent.id, scheduledEvent)
        (client as? DiscordWebSocketClient)?.let { 
            when(scheduledEvent.status) {
                ACTIVE -> it.handleEvent(ScheduledEventStartEvent(scheduledEvent))
                COMPLETED -> it.handleEvent(ScheduledEventCompleteEvent(scheduledEvent))
                CANCELED -> it.handleEvent(ScheduledEventCancelEvent(scheduledEvent))
                else -> Unit
            }
        }
        return ScheduledEventUpdate(scheduledEvent, oldScheduledEvent)
    }

}

class ScheduledEventDeleteHandler(val client: Client) : InternalEventHandler<ScheduledEventDelete> {

    override suspend fun handle(data: JsonObject): ScheduledEventDelete {
        val scheduledEvent = ScheduledEventSerializer.deserialize(data, client)
        scheduledEvent.guild.cache?.cacheManager?.guildScheduledEventCache?.remove(scheduledEvent.id)
        return ScheduledEventDelete(scheduledEvent)
    }

}