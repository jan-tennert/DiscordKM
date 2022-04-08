/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.serialization.serializers

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.channels.guild.StageChannel
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannel
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PrivacyLevel
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEvent
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEventCacheEntry
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEventCacheEntryImpl
import io.github.jan.discordkm.api.entities.guild.stage.StageInstance
import io.github.jan.discordkm.internal.serialization.BaseEntitySerializer
import io.github.jan.discordkm.internal.utils.get
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.isoTimestamp
import io.github.jan.discordkm.internal.utils.snowflake
import io.github.jan.discordkm.internal.utils.string
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

internal object ScheduledEventSerializer : BaseEntitySerializer<ScheduledEventCacheEntry> {
    override fun deserialize(data: JsonObject, value: DiscordClient): ScheduledEventCacheEntry {
        val guild = Guild(data["guild_id"]!!.snowflake, value)
        val channel = data["channel_id", true]?.snowflake?.let { VoiceChannel(it, guild) }
        return ScheduledEventCacheEntryImpl(
            data["id"]!!.snowflake,
            guild,
            channel,
            creator = User(data["creator_id"]!!.snowflake, value),
            data["name"]!!.string,
            data["description", true]?.string,
            data["scheduled_start_time"]!!.isoTimestamp,
            data["scheduled_end_time", true]?.isoTimestamp,
            PrivacyLevel[data["privacy_level"]!!.int],
            ScheduledEvent.EventStatus[data["status"]!!.int],
            ScheduledEvent.EntityType[data["entity_type"]!!.int],
            data["entity_id", true]?.let { StageInstance(it.snowflake, channel as StageChannel) },
            data["user_count", true]?.int ?: 0,
            data["metadata", true]?.jsonObject?.let {
                val location = it["location", true]?.string
                ScheduledEvent.EventMetadata(location)
            },
            data["image", true]?.string
        )
    }
}