/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.guild.channels.modifier

import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.guild.channels.PermissionOverride
import io.github.jan.discordkm.entities.guild.channels.VoiceChannel
import io.github.jan.discordkm.entities.misc.LimitedInt
import io.github.jan.discordkm.utils.putJsonObject
import io.github.jan.discordkm.utils.putOptional
import kotlinx.serialization.json.buildJsonObject

class VoiceChannelModifier : NonCategoryModifier<VoiceChannel> {

    override var name: String? = null

    override var position: Int? = null

    override var permissionOverrides: MutableList<PermissionOverride> = mutableListOf()

    override var parentId: Snowflake? = null

    var bitrate: Int by LimitedInt(8000, 96000)

    var userLimit: Int by LimitedInt(0, 99)

    var rtcRegion: String? = null

    var videoQualityMode: VoiceChannel.VideoQualityMode? = null

    override fun build() = buildJsonObject {
        putOptional("bitrate", bitrate)
        putOptional("user_limit", userLimit)
        putOptional("rtc_region", rtcRegion)
        putOptional("video_quality_mode", videoQualityMode?.ordinal?.plus(1))
        putJsonObject(super.build())
    }
}