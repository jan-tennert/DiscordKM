/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.guild.channels

import io.github.jan.discordkm.entities.channels.IParent
import io.github.jan.discordkm.entities.channels.Invitable
import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.entities.guild.channels.modifier.VoiceChannelModifier
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractGuildEntity
import io.github.jan.discordkm.utils.getOrNull
import io.github.jan.discordkm.utils.getOrThrow
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.json.JsonObject

open class VoiceChannel(guild: Guild, data: JsonObject) : GuildChannel(guild, data), Invitable, IParent {

    val userLimit = data.getOrThrow<Int>("user_limit")

    val regionId = data.getOrNull<String>("rtc_region")

    val bitrate = data.getOrThrow<Int>("bitrate")

    val videoQualityMode = if(data.getOrNull<Int>("video_quality_mode") != null) VideoQualityMode.values().first { it.ordinal == data.getOrNull<Int>("video_quality_mode") } else VideoQualityMode.AUTO

    /**
     * Modifies this voice channel
     */
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