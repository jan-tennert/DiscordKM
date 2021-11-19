package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEvent
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEvent.EventStatus.*
import io.github.jan.discordkm.api.events.ScheduledEventCancelEvent
import io.github.jan.discordkm.api.events.ScheduledEventCompleteEvent
import io.github.jan.discordkm.api.events.ScheduledEventCreateEvent
import io.github.jan.discordkm.api.events.ScheduledEventDeleteEvent
import io.github.jan.discordkm.api.events.ScheduledEventStartEvent
import io.github.jan.discordkm.api.events.ScheduledEventUpdateEvent
import io.github.jan.discordkm.api.events.ScheduledEventUserAddEvent
import io.github.jan.discordkm.api.events.ScheduledEventUserRemoveEvent
import io.github.jan.discordkm.internal.serialization.serializers.ScheduledEventSerializer
import io.github.jan.discordkm.internal.utils.snowflake
import kotlinx.serialization.json.JsonObject

class ScheduledEventCreateHandler(val client: Client) : InternalEventHandler<ScheduledEventCreateEvent> {

    override suspend fun handle(data: JsonObject): ScheduledEventCreateEvent {
        val scheduledEvent = ScheduledEventSerializer.deserialize(data, client)
        scheduledEvent.guild.cache?.cacheManager?.guildScheduledEventCache?.set(scheduledEvent.id, scheduledEvent)
        return ScheduledEventCreateEvent(scheduledEvent)
    }

}

class ScheduledEventUpdateHandler(val client: Client) : InternalEventHandler<ScheduledEventUpdateEvent> {

    override suspend fun handle(data: JsonObject): ScheduledEventUpdateEvent {
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
        return ScheduledEventUpdateEvent(scheduledEvent, oldScheduledEvent)
    }

}

class ScheduledEventDeleteHandler(val client: Client) : InternalEventHandler<ScheduledEventDeleteEvent> {

    override suspend fun handle(data: JsonObject): ScheduledEventDeleteEvent {
        val scheduledEvent = ScheduledEventSerializer.deserialize(data, client)
        scheduledEvent.guild.cache?.cacheManager?.guildScheduledEventCache?.remove(scheduledEvent.id)
        (client as? DiscordWebSocketClient)?.let {
            when(scheduledEvent.status) {
                COMPLETED -> it.handleEvent(ScheduledEventCompleteEvent(scheduledEvent))
                CANCELED -> it.handleEvent(ScheduledEventCancelEvent(scheduledEvent))
                else -> Unit
            }
        }
        return ScheduledEventDeleteEvent(scheduledEvent)
    }

}

class ScheduledEventUserAddEventHandler(val client: Client) : InternalEventHandler<ScheduledEventUserAddEvent> {

    override suspend fun handle(data: JsonObject): ScheduledEventUserAddEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val scheduledEvent = ScheduledEvent(data["guild_scheduled_event_id"]!!.snowflake, guild)
        val user = User(data["user_id"]!!.snowflake, client)
        return ScheduledEventUserAddEvent(scheduledEvent, user, guild)
    }

}

class ScheduledEventUserRemoveEventHandler(val client: Client) : InternalEventHandler<ScheduledEventUserRemoveEvent> {

    override suspend fun handle(data: JsonObject): ScheduledEventUserRemoveEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val scheduledEvent = ScheduledEvent(data["guild_scheduled_event_id"]!!.snowflake, guild)
        val user = User(data["user_id"]!!.snowflake, client)
        return ScheduledEventUserRemoveEvent(scheduledEvent, user, guild)
    }

}