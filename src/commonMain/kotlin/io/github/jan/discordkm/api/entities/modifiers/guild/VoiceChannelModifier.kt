/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.modifiers.guild

import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannel
import io.github.jan.discordkm.api.entities.misc.NullableLimitedInt
import io.github.jan.discordkm.internal.utils.modify
import io.github.jan.discordkm.internal.utils.putJsonObject
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

class VoiceChannelModifier(private val type: ChannelType) : ParentalModifier() {

    /*
     * The bitrate used in this voice channel
     * (8000 - 96000)
     */
    var bitrate: Int? by NullableLimitedInt(8000, 96000)

    /*
     * The maximum amount of members allowed to join this voice channel.
     * (0 - 99)
     */
    var userLimit: Int? by NullableLimitedInt(0, 99)

    /*
     * The new voice region for this voice channel
     */
    var rtcRegion: String? = null

    /*
     * The quality for new video streams
     */
    var videoQualityMode: VoiceChannel.VideoQualityMode? = null

    override val data: JsonObject get() = super.data.modify {
        putOptional("bitrate", bitrate)
        putOptional("user_limit", userLimit)
        putOptional("rtc_region", rtcRegion)
        putOptional("video_quality_mode", videoQualityMode?.ordinal?.plus(1))
        putOptional("type", type.value)
    }

}