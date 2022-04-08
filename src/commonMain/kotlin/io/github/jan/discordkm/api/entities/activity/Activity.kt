/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
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

/*
 * An activity is shown for a discord user e.g. when they play a game or have a custom status
 */
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

    /*
     * An activity can have up to 2 buttons, which open an url when you click on them
     */
    @Serializable
    data class ActivityButton(val label: String, val url: String)

    /*
     * An activity can have a large image and a small image. There can be also a largeText and a smallText which is shown when you hover over the specific image
     */
    @Serializable
    data class Assets(
        @SerialName("large_image")
        val largeImage: String? = null,
        @SerialName("large_text")
        val largeText: String? = null,
        @SerialName("small_image")
        val smallImage: String? = null,
        @SerialName("small_text")
        val smallText: String? = null
    )

    /*
     * If the activity supports it, here you can see the current and max party size
     */
    @Serializable
    data class Party(
        val id: String? = null,
        private val size: List<Int> = emptyList(),
    ) {

        val currentSize = size.getOrNull(0)

        val maxSize = size.getOrNull(1)

    }

    /*
     * If the activity supports it, here you can see the current timestamp from the activity
     */
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
        put("status", status.value)
        put("since", idleTime?.utc?.unixMillis)
        put("afk", afk)
        putJsonArray("activities") {
            activity?.let {
                add(it.build())
            }
        }
    }

    /*
     * Sets the activity to "Playing [name]"
     * @param name The new activity name
     */
    fun playing(name: String) = Presence(ActivityType.PLAYING, name)

    /*
     * Sets the activity to "Streaming [name]"
     * @param name The new activity name
     * @param url A youtube or twitch url
     */
    fun streaming(name: String, url: String) = Presence(ActivityType.STREAMING, name, url)

    /*
     * Sets the activity to "Listening to [name]"
     * @param name The new activity name
     */
    fun listening(name: String) = Presence(ActivityType.LISTENING, name)

    /*
     * Sets the activity to "Watching [name]"
     * @param name The new activity name
     */
    fun watching(name: String) = Presence(ActivityType.WATCHING, name)

    /*
     * Sets the activity to "Competing in [name]"
     * @param name The new activity name
     */
    fun competing(name: String) = Presence(ActivityType.COMPETING, name)

}

