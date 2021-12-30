package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.guild.StageChannel
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannel
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.scheduled.event.External
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

    /**
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

    /**
     * Retrieves a scheduled event by its id
     */
    suspend fun retrieveScheduledEvent(id: Snowflake) = guild.client.buildRestAction<ScheduledEventCacheEntry> {
        route = Route.ScheduledEvent.GET_EVENT(guild.id, id).get()
        transform { ScheduledEventSerializer.deserialize(it.toJsonObject(), guild.client) }
    }

    /**
     * Creates a new scheduled event
     * @param type [External], [VoiceChannel] or [StageChannel]
     */
    suspend fun <M : ScheduledEventModifier, T : ScheduledEventModifiable<M>> create(type: T, modifier: M.() -> Unit) = guild.client.buildRestAction<ScheduledEventCacheEntry> {
        route = Route.ScheduledEvent.CREATE_EVENT(guild.id).post(type.build(modifier))
        transform { ScheduledEventSerializer.deserialize(it.toJsonObject(), guild.client) }
    }

}

class CacheScheduledEventContainer(guild: Guild, override val values: Collection<ScheduledEventCacheEntry>) : NameableSnowflakeContainer<ScheduledEventCacheEntry>, ScheduledEventContainer(guild)