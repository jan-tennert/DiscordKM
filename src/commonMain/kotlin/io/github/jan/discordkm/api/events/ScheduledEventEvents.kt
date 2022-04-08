/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
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

/*
 * Fired when a scheduled event was created
 *
 * Requires the intent [Intent.GUILD_SCHEDULED_EVENTS]
 */
class ScheduledEventCreateEvent(override val scheduledEvent: ScheduledEventCacheEntry) : ScheduledEventEvent, ScheduledEventCacheEntry by scheduledEvent

/*
 * Fired when a scheduled event was updated
 *
 * Requires the intent [Intent.GUILD_SCHEDULED_EVENTS]
 */
class ScheduledEventUpdateEvent(override val scheduledEvent: ScheduledEventCacheEntry, val oldScheduledEvent: ScheduledEventCacheEntry?) : ScheduledEventEvent, ScheduledEventCacheEntry by scheduledEvent

/*
 * Fired when a scheduled event was deleted
 *
 * Requires the intent [Intent.GUILD_SCHEDULED_EVENTS]
 */
class ScheduledEventDeleteEvent(override val scheduledEvent: ScheduledEventCacheEntry) : ScheduledEventEvent, ScheduledEventCacheEntry by scheduledEvent

/*
 * Fired when a scheduled event was started
 *
 * Requires the intent [Intent.GUILD_SCHEDULED_EVENTS]
 */
class ScheduledEventStartEvent(override val scheduledEvent: ScheduledEventCacheEntry) : ScheduledEventEvent, ScheduledEventCacheEntry by scheduledEvent

/*
 * Fired when a scheduled event was completed
 *
 * Requires the intent [Intent.GUILD_SCHEDULED_EVENTS]
 */
class ScheduledEventCompleteEvent(override val scheduledEvent: ScheduledEventCacheEntry) : ScheduledEventEvent, ScheduledEventCacheEntry by scheduledEvent
/*
 * Fired when a scheduled event was canceled
 *
 * Requires the intent [Intent.GUILD_SCHEDULED_EVENTS]
 */
class ScheduledEventCancelEvent(override val scheduledEvent: ScheduledEventCacheEntry) : ScheduledEventEvent, ScheduledEventCacheEntry by scheduledEvent

/*
 * Fired when a user clicks on the "interested" button on a scheduled event
 *
 * Requires the intent [Intent.GUILD_SCHEDULED_EVENTS]
 */
class ScheduledEventUserAddEvent(override val scheduledEvent: ScheduledEvent, val user: User, val guild: Guild) : ScheduledEventEvent {

    override val client: DiscordClient
        get() = scheduledEvent.client

}

/*
 * Fired when a user uninterested on a scheduled event
 *
 * Requires the intent [Intent.GUILD_SCHEDULED_EVENTS]
 */
class ScheduledEventUserRemoveEvent(override val scheduledEvent: ScheduledEvent, val user: User, val guild: Guild) : ScheduledEventEvent {

    override val client: DiscordClient
        get() = scheduledEvent.client

}