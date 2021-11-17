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

    /**
     * The bitrate used in this voice channel
     * (8000 - 96000)
     */
    var bitrate: Int? by NullableLimitedInt(8000, 96000)

    /**
     * The maximum amount of members allowed to join this voice channel.
     * (0 - 99)
     */
    var userLimit: Int? by NullableLimitedInt(0, 99)

    /**
     * The new voice region for this voice channel
     */
    var rtcRegion: String? = null

    /**
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