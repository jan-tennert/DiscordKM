/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild.stage

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.guild.StageChannel
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.GuildEntity
import io.github.jan.discordkm.api.entities.guild.PrivacyLevel
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEvent

interface StageInstanceCacheEntry : StageInstance, GuildEntity {

    /*
     * The topic of the stage instance
     */
    val topic: String

    /*
     * The privacy level of the stage instance
     */
    val privacyLevel: PrivacyLevel

    /*
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