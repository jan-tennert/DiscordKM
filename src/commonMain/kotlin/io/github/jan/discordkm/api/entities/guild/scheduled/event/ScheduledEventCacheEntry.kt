package io.github.jan.discordkm.api.entities.guild.scheduled.event

import com.soywiz.klock.DateTimeTz
import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannel
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PrivacyLevel
import io.github.jan.discordkm.api.entities.guild.stage.StageInstance
import io.github.jan.discordkm.internal.caching.CacheEntry
import io.github.jan.discordkm.internal.entities.DiscordImage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Represents a cached scheduled event
 */
interface ScheduledEventCacheEntry : ScheduledEvent, Nameable, CacheEntry {

    /**
     * The channel the event is going to be executed in (if type is [ScheduledEvent.EntityType.VOICE])
     */
    val channel: VoiceChannel?

    /**
     * The creator of the event
     */
    val creator: User

    /**
     * The description of the event
     */
    val description: String?

    /**
     * The time the scheduled event is going to start
     */
    val startTime: DateTimeTz

    /**
     * The time the scheduled event is going to end (if type is [ScheduledEvent.EntityType.EXTERNAL])
     */
    val endTime: DateTimeTz?

    /**
     * The privacy level of the event
     */
    val privacyLevel: PrivacyLevel

    /**
     * The status of the event
     */
    val status: ScheduledEvent.EventStatus

    /**
     * The type of the scheduled event
     */
    val entityType: ScheduledEvent.EntityType

    /**
     * The associated stage instance (if type is [ScheduledEvent.EntityType.STAGE_INSTANCE]
     */
    val entity: StageInstance?

    /**
     * The amount of users participating in the event
     */
    val userCount: Int

    /**
     * The event metadata
     */
    val metadata: ScheduledEvent.EventMetadata?

    /**
     * The cover image of the event
     */
    val coverImageUrl: String?

}

internal class ScheduledEventCacheEntryImpl(
    override val id: Snowflake,
    override val guild: Guild,
    override val channel: VoiceChannel?,
    override val creator: User,
    override val name: String,
    override val description: String?,
    override val startTime: DateTimeTz,
    override val endTime: DateTimeTz?,
    override val privacyLevel: PrivacyLevel,
    override val status: ScheduledEvent.EventStatus,
    override val entityType: ScheduledEvent.EntityType,
    override val entity: StageInstance?,
    override var userCount: Int,
    override val metadata: ScheduledEvent.EventMetadata?,
    coverImageHash: String?
) : ScheduledEventCacheEntry {

    private val mutex = Mutex()

    override val coverImageUrl = coverImageHash?.let { DiscordImage.eventCoverImage(id, it) }

    internal suspend fun addUser() {
        mutex.withLock {
            userCount++
        }
    }

    internal suspend fun removeUser() {
        mutex.withLock {
            userCount--
        }
    }

    override fun toString(): String = "ScheduledEventCacheEntry(id=$id, guildId=${guild.id}, name=$name)"
    override fun hashCode() = id.hashCode()
    override fun equals(other: Any?): Boolean = other is ScheduledEvent && other.id == id && other.guild.id == guild.id

}