package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEvent
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEventCacheEntry

interface ScheduledEventEvent : Event {

    val scheduledEvent: ScheduledEventCacheEntry
    override val client: Client
        get() = scheduledEvent.client

}

class ScheduledEventCreate(override val scheduledEvent: ScheduledEventCacheEntry) : ScheduledEventEvent
class ScheduledEventUpdate(override val scheduledEvent: ScheduledEventCacheEntry, val oldScheduledEvent: ScheduledEventCacheEntry?) : ScheduledEventEvent
class ScheduledEventDelete(override val scheduledEvent: ScheduledEventCacheEntry) : ScheduledEventEvent

//context specific
class ScheduledEventStartEvent(override val scheduledEvent: ScheduledEventCacheEntry) : ScheduledEventEvent
class ScheduledEventCompleteEvent(override val scheduledEvent: ScheduledEventCacheEntry) : ScheduledEventEvent
class ScheduledEventCancelEvent(override val scheduledEvent: ScheduledEventCacheEntry) : ScheduledEventEvent

//user add