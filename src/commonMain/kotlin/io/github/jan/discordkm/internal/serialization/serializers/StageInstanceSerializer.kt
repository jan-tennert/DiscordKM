/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
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

internal object StageInstanceSerializer : BaseEntitySerializer<StageInstanceCacheEntry> {

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