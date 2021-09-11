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