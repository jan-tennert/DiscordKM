/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.modifiers.guild

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.internal.utils.modify
import io.github.jan.discordkm.internal.utils.putJsonObject
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.JsonObject

class TextChannelModifier : ParentalModifier(), MessageChannelModifier {

    /*
     * Whether this channel should be marked as a NSFWChannel.
     */
    var nsfw: Boolean? = null

    /*
     * Sets the new topic for the text channel
     */
    var topic: String? = null

    private var type: ChannelType? = null

    override val isThread: Boolean
        get() = false
    override var slowModeTime: TimeSpan? = null
    override var autoArchiveDuration: Thread.ThreadDuration? = null

    override val data: JsonObject
        get() = super<MessageChannelModifier>.data.modify {
            putOptional("default_auto_archive_duration", autoArchiveDuration?.duration?.minutes?.toInt())
            putOptional("rate_limit_per_user", slowModeTime?.seconds?.toInt())
            putOptional("topic", topic)
            putOptional("nsfw", nsfw)
            putOptional("type", type?.value)
            putJsonObject(super<ParentalModifier>.data)
        }

    fun convertToNewsChannel() { type = ChannelType.GUILD_NEWS }
    fun convertToTextChannel() { type = ChannelType.GUILD_TEXT }

}