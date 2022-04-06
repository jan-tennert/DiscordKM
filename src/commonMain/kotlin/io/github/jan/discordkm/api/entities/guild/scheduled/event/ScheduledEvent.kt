package io.github.jan.discordkm.api.entities.guild.scheduled.event

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.GuildEntity
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.caching.CacheEntity
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.ScheduledEventSerializer
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import io.github.jan.discordkm.internal.utils.modify
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

sealed interface ScheduledEvent : SnowflakeEntity, GuildEntity, CacheEntity {

    override val cache: ScheduledEventCacheEntry?
        get() = guild.cache?.scheduledEvents?.get(id)

    /**
     * Deletes this scheduled event
     */
    suspend fun delete() = client.buildRestAction<Unit> {
        route = Route.ScheduledEvent.DELETE_EVENT(guild.id, id).delete()
    }

    /**
     * Retrieves the users who are interested in this scheduled event
     * @param limit The maximum amount of users to retrieve. 1-100
     */
    suspend fun retrieveUsers(limit: Int = 100, before: Snowflake? = null, after: Snowflake? = null) = client.buildRestAction<List<User>> {
        route = Route.ScheduledEvent.GET_USERS(guild.id, id).get {
            put("limit", limit)
            putOptional("before", before?.string)
            putOptional("after", after?.string)
        }
        transform {
            it.toJsonArray().map { u -> User(u.jsonObject["user"]!!.jsonObject, client) }
        }
        check {
            if(limit < 1 || limit > 100) throw IllegalArgumentException("Limit must be between 1 and 100")
        }
    }

    suspend fun <M : ScheduledEventModifier, T : ScheduledEventModifiable<M>>modify(type: T, status: EventStatus? = null, reason: String? = null, modifier: M.() -> Unit) = client.buildRestAction<ScheduledEventCacheEntry> {
        route = Route.ScheduledEvent.MODIFY_EVENT(guild.id, id).patch(type.build(modifier).modify { putOptional("status", status?.value) })
        transform { ScheduledEventSerializer.deserialize(it.toJsonObject(), client) }
        this.reason = reason
    }

    private suspend fun setStatus(status: EventStatus) = client.buildRestAction<ScheduledEventCacheEntry> {
        route = Route.ScheduledEvent.MODIFY_EVENT(guild.id, id).patch(buildJsonObject  {put("status", status.value) })
        transform { ScheduledEventSerializer.deserialize(it.toJsonObject(), client) }
    }

    /**
     * Starts this event
     */
    suspend fun start() = setStatus(EventStatus.ACTIVE)

    /**
     * Cancels this event
     */
    suspend fun cancel() = setStatus(EventStatus.CANCELED)

    enum class EventStatus : EnumWithValue<Int> {
        SCHEDULED, ACTIVE, COMPLETED, CANCELED;

        override val value: Int
            get() = ordinal + 1

        companion object : EnumWithValueGetter<EventStatus, Int>(values())
    }

    enum class EntityType : EnumWithValue<Int> {
        NONE, STAGE_INSTANCE, VOICE, EXTERNAL;

        override val value: Int
            get() = ordinal

        companion object : EnumWithValueGetter<EntityType, Int>(values())
    }

    class EventMetadata(
        val location: String?
    )

    companion object {
        operator fun invoke(id: Snowflake, guild: Guild): ScheduledEvent = ScheduledEventImpl(id, guild)
    }

}

internal class ScheduledEventImpl(override val id: Snowflake, override val guild: Guild) : ScheduledEvent {

    override fun toString(): String = "ScheduledEvent(id=$id, guildId=${guild.id})"
    override fun hashCode() = id.hashCode()
    override fun equals(other: Any?): Boolean = other is ScheduledEvent && other.id == id && other.guild.id == guild.id

}