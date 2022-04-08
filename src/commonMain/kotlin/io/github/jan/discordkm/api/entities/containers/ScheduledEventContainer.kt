/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEventCacheEntry
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEventModifiable
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEventModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.ScheduledEventSerializer
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.jsonObject

open class ScheduledEventContainer(val guild: Guild) {

    /*
     * Retrieves all scheduled events for the guild.
     */
    suspend fun retrieveScheduledEvents(withUserCount: Boolean = false) = guild.client.buildRestAction<List<ScheduledEventCacheEntry>> {
        route = Route.ScheduledEvent.GET_EVENTS(guild.id).get {
            put("with_user_count", withUserCount)
        }
        transform {
            it.toJsonArray().map { e -> ScheduledEventSerializer.deserialize(e.jsonObject, guild.client) }
        }
    }

    /*
     * Retrieves a scheduled event by its id
     */
    suspend fun retrieveScheduledEvent(id: Snowflake, withUserCount: Boolean = false) = guild.client.buildRestAction<ScheduledEventCacheEntry> {
        route = Route.ScheduledEvent.GET_EVENT(guild.id, id).get {
            put("with_user_count", withUserCount)
        }
        transform { ScheduledEventSerializer.deserialize(it.toJsonObject(), guild.client) }
    }

    /*
     * Creates a new scheduled event
     * @param type [External], [VoiceChannel] or [StageChannel]
     */
    suspend fun <M : ScheduledEventModifier, T : ScheduledEventModifiable<M>> create(type: T, modifier: M.() -> Unit) = guild.client.buildRestAction<ScheduledEventCacheEntry> {
        route = Route.ScheduledEvent.CREATE_EVENT(guild.id).post(type.createScheduledEvent(modifier))
        transform { ScheduledEventSerializer.deserialize(it.toJsonObject(), guild.client) }
    }

}

class CacheScheduledEventContainer(guild: Guild, override val values: Collection<ScheduledEventCacheEntry>) : NameableSnowflakeContainer<ScheduledEventCacheEntry>, ScheduledEventContainer(guild)