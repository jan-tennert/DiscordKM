package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEventCacheEntry
import io.github.jan.discordkm.api.entities.modifiers.guild.BaseScheduledEventModifier
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

    /**
     * Retrieves all scheduled events for the guild.
     */
    suspend fun retrieveScheduledEvents(withUserCount: Boolean = false) = guild.client.buildRestAction<List<ScheduledEventCacheEntry>> {
        route = Route.ScheduledEvent.GET_EVENTS(guild.id).get {
            put("with_user_count", withUserCount)
        }
        transform {
            it.toJsonArray().map { e -> ScheduledEventSerializer.deserialize(e.jsonObject, client) }
        }
    }

    /**
     * Retrieves a scheduled event by its id
     */
    suspend fun retrieveScheduledEvent(id: Snowflake) = guild.client.buildRestAction<ScheduledEventCacheEntry> {
        route = Route.ScheduledEvent.GET_EVENT(guild.id, id).get()
        transform { ScheduledEventSerializer.deserialize(it.toJsonObject(), client) }
    }

    /**
     * Creates a new scheduled event
     */
    suspend fun create(builder: BaseScheduledEventModifier.() -> Unit) = guild.client.buildRestAction<ScheduledEventCacheEntry> {
        route = Route.ScheduledEvent.CREATE_EVENT(guild.id).post(BaseScheduledEventModifier().apply(builder).data)
        transform { ScheduledEventSerializer.deserialize(it.toJsonObject(), client) }
    }

}

class CacheScheduledEventContainer(guild: Guild, override val values: Collection<ScheduledEventCacheEntry>) : NameableSnowflakeContainer<ScheduledEventCacheEntry>, ScheduledEventContainer(guild)