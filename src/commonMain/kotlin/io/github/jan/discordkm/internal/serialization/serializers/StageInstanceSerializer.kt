package io.github.jan.discordkm.internal.serialization.serializers

import io.github.jan.discordkm.api.entities.channels.guild.StageChannel
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PrivacyLevel
import io.github.jan.discordkm.api.entities.guild.StageInstance
import io.github.jan.discordkm.api.entities.guild.StageInstanceCacheEntry
import io.github.jan.discordkm.internal.serialization.BaseEntitySerializer
import io.github.jan.discordkm.internal.serialization.GuildEntitySerializer
import io.github.jan.discordkm.internal.utils.boolean
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.snowflake
import io.github.jan.discordkm.internal.utils.string
import kotlinx.serialization.json.JsonObject

object StageInstanceSerializer : BaseEntitySerializer<StageInstanceCacheEntry> {

    override fun deserialize(data: JsonObject, value: Client): StageInstanceCacheEntry {
        val guild = Guild(data["guild_id"]!!.snowflake, value)
        return StageInstanceCacheEntry(
            id = data["id"]!!.snowflake,
            guild = guild,
            stageChannel = StageChannel(data["channel_id"]!!.snowflake, guild),
            topic = data["topic"]!!.string,
            privacyLevel = PrivacyLevel[data["privacy_level"]!!.int],
            isDiscoveryEnabled = !(data["discovery_disabled"]!!.boolean),
            )
    }

}