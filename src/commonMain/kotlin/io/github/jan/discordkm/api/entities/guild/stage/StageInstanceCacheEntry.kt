package io.github.jan.discordkm.api.entities.guild.stage

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.guild.StageChannel
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.GuildEntity
import io.github.jan.discordkm.api.entities.guild.PrivacyLevel
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEvent

interface StageInstanceCacheEntry : StageInstance, GuildEntity {

    /**
     * The topic of the stage instance
     */
    val topic: String

    /**
     * The privacy level of the stage instance
     */
    val privacyLevel: PrivacyLevel

    /**
     * The associated scheduled event, if [scheduledEvent] is of type [ScheduledEvent.EntityType.STAGE_INSTANCE]
     */
    val scheduledEvent: ScheduledEvent?

    override val client: DiscordClient
        get() = stageChannel.client

}

internal class StageInstanceCacheEntryImpl(
    override val guild: Guild,
    override val id: Snowflake,
    override val topic: String,
    override val stageChannel: StageChannel,
    override val privacyLevel: PrivacyLevel,
    override val scheduledEvent: ScheduledEvent?
) : StageInstanceCacheEntry {

    override fun toString(): String = "StageInstanceCacheEntry(id=$id, guildId=${guild.id}, stageChannelId=${stageChannel.id}, topic=$topic)"
    override fun hashCode() = id.hashCode()
    override fun equals(other: Any?): Boolean = other is StageInstance && other.id == id && other.stageChannel.id == stageChannel.id

}