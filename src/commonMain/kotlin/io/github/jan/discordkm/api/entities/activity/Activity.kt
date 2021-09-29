package io.github.jan.discordkm.api.entities.activity

import com.soywiz.klock.DateTimeTz
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.internal.utils.UnixDateTimeSerializer
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

@Serializable
data class Activity(
    val name: String,
    val type: ActivityType,
    val url: String? = null,
    @Serializable(with = UnixDateTimeSerializer::class)
    @SerialName("created_at")
    val createdAt: DateTimeTz,
    val timestamps: Timestamps? = null,
    @SerialName("application_id")
    val applicationId: Snowflake? = null,
    val details: String? = null,
    val state: String? = null,
    val emoji: Emoji? = null,
    val party: Party? = null,
    val assets: Assets? = null,
    @SerialName("instance")
    val isInstance: Boolean = false,
    val buttons: List<ActivityButton> = emptyList()
) {

    @Serializable
    data class ActivityButton(val label: String, val url: String)

    @Serializable
    data class Assets(
        val largeImage: String? = null,
        val largeText: String? = null,
        val smallImage: String? = null,
        val smallText: String? = null
    )

    @Serializable
    data class Party(
        val id: String?,
        private val size: List<Int> = emptyList(),
    ) {

        val currentSize = size.getOrNull(0)

        val maxSize = size.getOrNull(1)

    }

    @Serializable
    data class Timestamps(
        @Serializable(with = UnixDateTimeSerializer::class)
        val start: DateTimeTz? = null,
        @Serializable(with = UnixDateTimeSerializer::class)
        val end: DateTimeTz? = null)

}

class Presence internal constructor(val type: ActivityType, val name: String, private val url: String? = null) {

    fun build() = buildJsonObject {
        put("name", name)
        put("type", type.ordinal)
        putOptional("url", url)
    }

}

class PresenceModifier(var status: PresenceStatus = PresenceStatus.ONLINE, var activity: Presence? = null, var afk: Boolean = false, var idleTime: DateTimeTz? = null) {

    fun build() = buildJsonObject {
        put("status", status.status)
        put("since", idleTime?.utc?.unixMillis)
        put("afk", afk)
        putJsonArray("activities") {
            activity?.let {
                add(it.build())
            }
        }
    }

    fun playing(name: String) = Presence(ActivityType.PLAYING, name)
    fun streaming(name: String, url: String) = Presence(ActivityType.STREAMING, name, url)
    fun listening(name: String) = Presence(ActivityType.LISTENING, name)
    fun watching(name: String) = Presence(ActivityType.WATCHING, name)
    fun competing(name: String) = Presence(ActivityType.COMPETING, name)

}

