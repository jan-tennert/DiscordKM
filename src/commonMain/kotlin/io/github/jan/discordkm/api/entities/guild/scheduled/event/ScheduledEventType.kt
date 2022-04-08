/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild.scheduled.event

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.ISO8601
import com.soywiz.klock.hours
import com.soywiz.klock.minutes
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.PrivacyLevel
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface ScheduledEventModifiable <T : ScheduledEventModifier> {

    fun createScheduledEvent(modifier: T.() -> Unit) : JsonObject

}

sealed interface ScheduledEventModifier {

    var startTime: DateTimeTz
    var description: String
    var name: String

    fun build() : JsonObject

}

class ScheduledEventVoiceChannel(private val stageChannel: Boolean) : ScheduledEventModifier {

    override var startTime: DateTimeTz = DateTimeTz.nowLocal().plus(20.minutes)
    override var description: String = ""
    override var name: String = ""

    var channelId: Snowflake? = null

    override fun build() = buildJsonObject {
        put("entity_type", if(stageChannel) ScheduledEvent.EntityType.STAGE_INSTANCE.value else ScheduledEvent.EntityType.VOICE.value)
        put("description", description)
        put("name", name)
        put("scheduled_start_time", ISO8601.DATETIME_UTC_COMPLETE.format(startTime))
        put("privacy_level", PrivacyLevel.GUILD_ONLY.value)
        put("channel_id", channelId?.string)
    }

}

class ScheduledEventExternal : ScheduledEventModifier {

    override var startTime: DateTimeTz = DateTimeTz.nowLocal().plus(20.minutes)
    override var description: String = ""
    override var name: String = ""
    var endTime: DateTimeTz = DateTimeTz.nowLocal().plus(1.hours)
    var location: String = ""

    override fun build() = buildJsonObject {
        put("entity_type", ScheduledEvent.EntityType.EXTERNAL.value)
        put("description", description)
        put("name", name)
        put("scheduled_start_time", ISO8601.DATETIME_UTC_COMPLETE.format(startTime))
        put("scheduled_end_time", ISO8601.DATETIME_UTC_COMPLETE.format(endTime))
        put("entity_metadata", buildJsonObject {
            put("location", location)
        })
        put("privacy_level", PrivacyLevel.GUILD_ONLY.value)
    }

}

object External : ScheduledEventModifiable<ScheduledEventExternal> {

    override fun createScheduledEvent(modifier: ScheduledEventExternal.() -> Unit) = ScheduledEventExternal().apply(modifier).build()

}