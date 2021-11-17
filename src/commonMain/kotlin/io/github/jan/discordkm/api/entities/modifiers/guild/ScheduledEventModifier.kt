package io.github.jan.discordkm.api.entities.modifiers.guild

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.ISO8601
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.PrivacyLevel
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEvent
import io.github.jan.discordkm.api.entities.modifiers.BaseModifier
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

open class BaseScheduledEventModifier: BaseModifier {

    private var channelId: Snowflake? = null
    private var location: String? = null
    var name: String? = null
    var privacyLevel = PrivacyLevel.GUILD_ONLY
    var startTime: DateTimeTz? = null
    var endTime: DateTimeTz? = null
    var description: String? = null
    private var entityType: ScheduledEvent.EntityType? = null
    internal var status: ScheduledEvent.EventStatus? = null

    fun stageChannel(id: Snowflake) {
        channelId = id
        entityType = ScheduledEvent.EntityType.STAGE_INSTANCE
    }

    fun voiceChannel(id: Snowflake) {
        channelId = id
        entityType = ScheduledEvent.EntityType.VOICE
    }

    fun external(location: String) {
        this.location = location
        entityType = ScheduledEvent.EntityType.EXTERNAL
    }

    override val data: JsonObject
        get() = buildJsonObject {
            putOptional("channel_id", channelId?.string)
            location?.let {
                putJsonObject("entity_metadata") {
                    putOptional("location", location)
                }
            }
            putOptional("name", name)
            putOptional("privacy_level", privacyLevel.value)
            putOptional("scheduled_start_time", startTime?.let { ISO8601.DATETIME_UTC_COMPLETE.format(it) })
            putOptional("scheduled_end_time", endTime?.let { ISO8601.DATETIME_UTC_COMPLETE.format(it) })
            putOptional("description", description)
            putOptional("entity_type", entityType?.value)
            putOptional("status", status?.value)
        }
}

class ScheduledEventModifier : BaseScheduledEventModifier() {

    fun cancel() { status = ScheduledEvent.EventStatus.CANCELED }
    fun start() { status = ScheduledEvent.EventStatus.ACTIVE }

}