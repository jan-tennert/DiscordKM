package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.clients.Intent
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEvent
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEventCacheEntry

interface ScheduledEventEvent : Event {

    val scheduledEvent: ScheduledEvent

}

/**
 * Fired when a scheduled event was created
 *
 * Requires the intent [Intent.GUILD_SCHEDULED_EVENTS]
 */
class ScheduledEventCreateEvent(override val scheduledEvent: ScheduledEventCacheEntry) : ScheduledEventEvent, ScheduledEventCacheEntry by scheduledEvent

/**
 * Fired when a scheduled event was updated
 *
 * Requires the intent [Intent.GUILD_SCHEDULED_EVENTS]
 */
class ScheduledEventUpdateEvent(override val scheduledEvent: ScheduledEventCacheEntry, val oldScheduledEvent: ScheduledEventCacheEntry?) : ScheduledEventEvent, ScheduledEventCacheEntry by scheduledEvent

/**
 * Fired when a scheduled event was deleted
 *
 * Requires the intent [Intent.GUILD_SCHEDULED_EVENTS]
 */
class ScheduledEventDeleteEvent(override val scheduledEvent: ScheduledEventCacheEntry) : ScheduledEventEvent, ScheduledEventCacheEntry by scheduledEvent

/**
 * Fired when a scheduled event was started
 *
 * Requires the intent [Intent.GUILD_SCHEDULED_EVENTS]
 */
class ScheduledEventStartEvent(override val scheduledEvent: ScheduledEventCacheEntry) : ScheduledEventEvent, ScheduledEventCacheEntry by scheduledEvent

/**
 * Fired when a scheduled event was completed
 *
 * Requires the intent [Intent.GUILD_SCHEDULED_EVENTS]
 */
class ScheduledEventCompleteEvent(override val scheduledEvent: ScheduledEventCacheEntry) : ScheduledEventEvent, ScheduledEventCacheEntry by scheduledEvent
/**
 * Fired when a scheduled event was canceled
 *
 * Requires the intent [Intent.GUILD_SCHEDULED_EVENTS]
 */
class ScheduledEventCancelEvent(override val scheduledEvent: ScheduledEventCacheEntry) : ScheduledEventEvent, ScheduledEventCacheEntry by scheduledEvent

/**
 * Fired when a user clicks on the "interested" button on a scheduled event
 *
 * Requires the intent [Intent.GUILD_SCHEDULED_EVENTS]
 */
class ScheduledEventUserAddEvent(override val scheduledEvent: ScheduledEvent, val user: User, val guild: Guild) : ScheduledEventEvent {

    override val client: DiscordClient
        get() = scheduledEvent.client

}

/**
 * Fired when a user uninterested on a scheduled event
 *
 * Requires the intent [Intent.GUILD_SCHEDULED_EVENTS]
 */
class ScheduledEventUserRemoveEvent(override val scheduledEvent: ScheduledEvent, val user: User, val guild: Guild) : ScheduledEventEvent {

    override val client: DiscordClient
        get() = scheduledEvent.client

}