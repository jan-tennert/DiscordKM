package com.github.jan.discordkm.entities.guild.channels

import com.github.jan.discordkm.entities.guild.Guild
import com.github.jan.discordkm.utils.getOrNull
import com.github.jan.discordkm.utils.getOrThrow
import kotlinx.serialization.json.JsonObject

open class VoiceChannel(guild: Guild, data: JsonObject) : GuildChannel(guild, data) {

    val userLimit = data.getOrThrow<Int>("user_limit")

    val regionId = data.getOrNull<String>("rtc_region")

    val bitrate = data.getOrThrow<Int>("bitrate")

    val videoQualityMode = if(data.getOrNull<Int>("video_quality_mode") != null) VideoQualityMode.values().first { it.ordinal == data.getOrNull<Int>("video_quality_mode") } else VideoQualityMode.AUTO

    enum class VideoQualityMode {
        AUTO,
        FULL
    }

    /*@Serializable
    data class Region(
        val id: String,
        val name: String,
        @SerialName("vip")
        @get:JvmName("isDeprecated")
        val isVip: Boolean,
        @get:JvmName("isDeprecated")
        @SerialName("deprecated")
        val isDeprecated: Boolean,
        @get:JvmName("isOptimal")
        @SerialName("optimal")
        val isOptimal: Boolean,
        @get:JvmName("isCustom")
        @SerialName("custom")
        val isCustom: Boolean
        )*/

}