package io.github.jan.discordkm.api.entities.guild.scheduled.event

import com.soywiz.klock.DateTimeTz
import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannel
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.GuildEntity
import io.github.jan.discordkm.api.entities.guild.PrivacyLevel
import io.github.jan.discordkm.api.entities.guild.StageInstance
import io.github.jan.discordkm.api.entities.modifiers.Modifiable
import io.github.jan.discordkm.api.entities.modifiers.guild.ScheduledEventModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.ScheduledEventSerializer
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.jsonObject

interface ScheduledEvent : SnowflakeEntity, GuildEntity, Modifiable<ScheduledEventModifier, ScheduledEventCacheEntry> {

    //modify
    //retrieve
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
    suspend fun retrieveUsers(limit: Int = 100) = client.buildRestAction<List<User>> {
        route = Route.ScheduledEvent.GET_USERS(guild.id, id).get {
            put("limit", limit)
        }
        transform {
            it.toJsonArray().map { u -> User(u.jsonObject, client) }
        }
        check {
            if(limit < 1 || limit > 100) throw IllegalArgumentException("Limit must be between 1 and 100")
        }
    }

    override suspend fun modify(modifier: ScheduledEventModifier.() -> Unit) = client.buildRestAction<ScheduledEventCacheEntry> {
        route = Route.ScheduledEvent.MODIFY_EVENT(guild.id, id).patch(ScheduledEventModifier().apply(modifier).data)
        transform { ScheduledEventSerializer.deserialize(it.toJsonObject(), client) }
    }

    /**
     * Starts this event
     */
    suspend fun start() = modify { start() }

    /**
     * Cancels this event
     */
    suspend fun cancel() = modify { cancel() }

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
        val speakers: List<User>,
        val location: String?
    )

}

/**
 * Represents a cached scheduled event
 * @param id The id of the scheduled event
 * @param guild The guild this scheduled event belongs to
 * @param name The name of the scheduled event
 * @param description The description of the scheduled event
 * @param startTime The time when the event starts
 * @param endTime The time when the event ends
 * @param status The status of the scheduled event
 * @param privacyLevel The privacy level of the scheduled event
 * @param entityType The type of entity this scheduled event is linked to
 * @param entity The entity this scheduled event is linked to (StageInstance for now)
 * @param metadata The metadata of the scheduled event
 * @param userCount The amount of users who are interested in this scheduled event
 */
class ScheduledEventCacheEntry(
    override val id: Snowflake,
    override val guild: Guild,
    val channel: VoiceChannel?,
    val creator: User,
    override val name: String,
    val description: String?,
    val startTime: DateTimeTz,
    val endTime: DateTimeTz?,
    val privacyLevel: PrivacyLevel,
    val status: ScheduledEvent.EventStatus,
    val entityType: ScheduledEvent.EntityType,
    val entity: StageInstance?,
    val userCount: Int,
    val metadata: ScheduledEvent.EventMetadata?
) : ScheduledEvent, Nameable