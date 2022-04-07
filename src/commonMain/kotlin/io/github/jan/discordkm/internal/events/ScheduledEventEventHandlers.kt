package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.clients.WSDiscordClient
import io.github.jan.discordkm.api.entities.clients.WSDiscordClientImpl
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.cacheManager
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEvent
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEvent.EventStatus.ACTIVE
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEvent.EventStatus.CANCELED
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEvent.EventStatus.COMPLETED
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEventCacheEntryImpl
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

internal class ScheduledEventCreateHandler(val client: DiscordClient) : InternalEventHandler<ScheduledEventCreateEvent> {

    override suspend fun handle(data: JsonObject): ScheduledEventCreateEvent {
        val scheduledEvent = ScheduledEventSerializer.deserialize(data, client)
        scheduledEvent.guild.cache?.cacheManager?.guildScheduledEventCache?.set(scheduledEvent.id, scheduledEvent)
        return ScheduledEventCreateEvent(scheduledEvent)
    }

}

internal class ScheduledEventUpdateHandler(val client: DiscordClient) : InternalEventHandler<ScheduledEventUpdateEvent> {

    override suspend fun handle(data: JsonObject): ScheduledEventUpdateEvent {
        val scheduledEvent = ScheduledEventSerializer.deserialize(data, client)
        val oldScheduledEvent = scheduledEvent.guild.cache?.scheduledEvents?.get(scheduledEvent.id)
        scheduledEvent.guild.cache?.cacheManager?.guildScheduledEventCache?.set(scheduledEvent.id, scheduledEvent)
        (client as? WSDiscordClient)?.let {
            when(scheduledEvent.status) {
                ACTIVE -> (client as WSDiscordClientImpl).handleEvent(ScheduledEventStartEvent(scheduledEvent))
                COMPLETED -> (client as WSDiscordClientImpl).handleEvent(ScheduledEventCompleteEvent(scheduledEvent))
                CANCELED -> (client as WSDiscordClientImpl).handleEvent(ScheduledEventCancelEvent(scheduledEvent))
                else -> Unit
            }
        }
        return ScheduledEventUpdateEvent(scheduledEvent, oldScheduledEvent)
    }

}

internal class ScheduledEventDeleteHandler(val client: DiscordClient) : InternalEventHandler<ScheduledEventDeleteEvent> {

    override suspend fun handle(data: JsonObject): ScheduledEventDeleteEvent {
        val scheduledEvent = ScheduledEventSerializer.deserialize(data, client)
        scheduledEvent.guild.cache?.cacheManager?.guildScheduledEventCache?.remove(scheduledEvent.id)
        (client as? WSDiscordClient)?.let {
            when(scheduledEvent.status) {
                COMPLETED -> (client as WSDiscordClientImpl).handleEvent(ScheduledEventCompleteEvent(scheduledEvent))
                CANCELED -> (client as WSDiscordClientImpl).handleEvent(ScheduledEventCancelEvent(scheduledEvent))
                else -> Unit
            }
        }
        return ScheduledEventDeleteEvent(scheduledEvent)
    }

}

internal class ScheduledEventUserAddEventHandler(val client: DiscordClient) : InternalEventHandler<ScheduledEventUserAddEvent> {

    override suspend fun handle(data: JsonObject): ScheduledEventUserAddEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val scheduledEvent = ScheduledEvent(data["guild_scheduled_event_id"]!!.snowflake, guild)
        (scheduledEvent.cache as? ScheduledEventCacheEntryImpl)?.addUser()
        val user = User(data["user_id"]!!.snowflake, client)
        return ScheduledEventUserAddEvent(scheduledEvent, user, guild)
    }

}

internal class ScheduledEventUserRemoveEventHandler(val client: DiscordClient) : InternalEventHandler<ScheduledEventUserRemoveEvent> {

    override suspend fun handle(data: JsonObject): ScheduledEventUserRemoveEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val scheduledEvent = ScheduledEvent(data["guild_scheduled_event_id"]!!.snowflake, guild)
        (scheduledEvent.cache as? ScheduledEventCacheEntryImpl)?.removeUser()
        val user = User(data["user_id"]!!.snowflake, client)
        return ScheduledEventUserRemoveEvent(scheduledEvent, user, guild)
    }

}