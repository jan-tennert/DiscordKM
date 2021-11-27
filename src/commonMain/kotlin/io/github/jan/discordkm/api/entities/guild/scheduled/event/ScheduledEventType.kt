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

    fun build(modifier: T.() -> Unit) : JsonObject

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

    override fun build(modifier: ScheduledEventExternal.() -> Unit) = ScheduledEventExternal().apply(modifier).build()

}