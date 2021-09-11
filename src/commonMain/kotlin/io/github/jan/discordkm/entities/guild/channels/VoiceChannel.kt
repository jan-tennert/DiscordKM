package io.github.jan.discordkm.entities.guild.channels

import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.entities.guild.channels.modifier.VoiceChannelModifier
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractGuildEntity
import io.github.jan.discordkm.utils.getOrNull
import io.github.jan.discordkm.utils.getOrThrow
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.json.JsonObject

open class VoiceChannel(guild: Guild, data: JsonObject) : GuildChannel(guild, data) {

    val userLimit = data.getOrThrow<Int>("user_limit")

    val regionId = data.getOrNull<String>("rtc_region")

    val bitrate = data.getOrThrow<Int>("bitrate")

    val videoQualityMode = if(data.getOrNull<Int>("video_quality_mode") != null) VideoQualityMode.values().first { it.ordinal == data.getOrNull<Int>("video_quality_mode") } else VideoQualityMode.AUTO

    open suspend fun modify(modifier: VoiceChannelModifier.() -> Unit = {}): VoiceChannel = client.buildRestAction<VoiceChannel> {
        action = RestAction.Action.patch("/channels/$id", VoiceChannelModifier().apply(modifier).build())
        transform {
            it.toJsonObject().extractGuildEntity(guild)
        }
        onFinish { guild.channelCache[id] = it }
    }

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