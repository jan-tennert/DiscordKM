package io.github.jan.discordkm.api.entities.activity

import com.soywiz.klock.DateTimeTz
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.SerializableEntity
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

class Activity(override val client: Client, override val data: JsonObject) : SerializableEntity {
}

class DiscordActivity internal constructor(val type: ActivityType, val name: String, private val url: String? = null) {

    fun build() = buildJsonObject {
        put("name", name)
        put("type", type.ordinal)
        putOptional("url", url)
    }

}

class ActivityModifier(var status: PresenceStatus = PresenceStatus.ONLINE, var activity: DiscordActivity? = null, var afk: Boolean = false, var idleTime: DateTimeTz? = null) {

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

    fun playing(name: String) = DiscordActivity(ActivityType.PLAYING, name)
    fun streaming(name: String, url: String) = DiscordActivity(ActivityType.STREAMING, name, url)
    fun listening(name: String) = DiscordActivity(ActivityType.LISTENING, name)
    fun watching(name: String) = DiscordActivity(ActivityType.WATCHING, name)
    fun competing(name: String) = DiscordActivity(ActivityType.COMPETING, name)

}

