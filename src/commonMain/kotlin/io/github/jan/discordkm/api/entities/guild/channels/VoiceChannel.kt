/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild.channels

import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.guild.channels.modifier.GuildChannelBuilder
import io.github.jan.discordkm.api.entities.guild.channels.modifier.VoiceChannelModifier
import io.github.jan.discordkm.api.entities.lists.retrieve
import io.github.jan.discordkm.internal.entities.channels.IParent
import io.github.jan.discordkm.internal.entities.channels.Invitable
import io.github.jan.discordkm.internal.serialization.UpdateVoiceStatePayload
import io.github.jan.discordkm.internal.utils.getOrDefault
import io.github.jan.discordkm.internal.utils.getOrNull

interface VoiceChannel : GuildChannel, Invitable, IParent {

    val userLimit: Int
        get() = data.getOrDefault("user_limit", 0)

    val regionId: String?
        get() = data.getOrNull<String>("rtc_region")

    val bitrate: Int
        get() = data.getOrDefault<Int>("bitrate", 0)

    val videoQualityMode: VideoQualityMode
        get() = if(data.getOrNull<Int>("video_quality_mode") != null) VideoQualityMode.values().first { it.ordinal == data.getOrNull<Int>("video_quality_mode") } else VideoQualityMode.AUTO

    suspend fun join() = if(client is DiscordWebSocketClient) {
        (client as DiscordWebSocketClient).gateways[0].send(UpdateVoiceStatePayload(guild.id, id, guild.selfMember.isMuted, guild.selfMember.isDeafened))
    } else {
        throw UnsupportedOperationException("You can't join a voice channel without having a gateway connection!")
    }

    /**
     * Modifies this voice channel
     */
    suspend fun modify(modifier: VoiceChannelModifier.() -> Unit = {}): VoiceChannel

    override suspend fun retrieve() = guild.channels.retrieve(id) as VoiceChannel

    companion object : GuildChannelBuilder<VoiceChannel, VoiceChannelModifier> {

        override fun create(builder: VoiceChannelModifier.() -> Unit) = VoiceChannelModifier(2).apply(builder).build()

    }

    enum class VideoQualityMode {
        AUTO,
        FULL
    }

}