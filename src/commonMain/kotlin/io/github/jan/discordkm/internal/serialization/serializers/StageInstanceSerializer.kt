package io.github.jan.discordkm.internal.serialization.serializers

import io.github.jan.discordkm.api.entities.channels.guild.StageChannel
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PrivacyLevel
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEvent
import io.github.jan.discordkm.api.entities.guild.stage.StageInstanceCacheEntry
import io.github.jan.discordkm.api.entities.guild.stage.StageInstanceCacheEntryImpl
import io.github.jan.discordkm.internal.serialization.BaseEntitySerializer
import io.github.jan.discordkm.internal.utils.get
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.snowflake
import io.github.jan.discordkm.internal.utils.string
import kotlinx.serialization.json.JsonObject

object StageInstanceSerializer : BaseEntitySerializer<StageInstanceCacheEntry> {

    override fun deserialize(data: JsonObject, value: DiscordClient): StageInstanceCacheEntry {
        val guild = Guild(data["guild_id"]!!.snowflake, value)
        return StageInstanceCacheEntryImpl(
            id = data["id"]!!.snowflake,
            guild = guild,
            stageChannel = StageChannel(data["channel_id"]!!.snowflake, guild),
            topic = data["topic"]!!.string,
            privacyLevel = PrivacyLevel[data["privacy_level"]!!.int],
            scheduledEvent = ScheduledEvent(data["guild_scheduled_event_id", true]!!.snowflake, guild)
            )
    }

}