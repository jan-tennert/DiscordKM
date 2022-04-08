/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.serialization.serializers

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannel
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.member.Member
import io.github.jan.discordkm.api.entities.guild.VoiceStateCacheEntry
import io.github.jan.discordkm.internal.serialization.GuildEntitySerializer
import io.github.jan.discordkm.internal.utils.boolean
import io.github.jan.discordkm.internal.utils.isoTimestamp
import io.github.jan.discordkm.internal.utils.snowflake
import io.github.jan.discordkm.internal.utils.string
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import io.github.jan.discordkm.internal.utils.get


internal object VoiceStateSerializer : GuildEntitySerializer<VoiceStateCacheEntry> {

    override fun deserialize(data: JsonObject, value: Guild) = VoiceStateCacheEntry(
        guild = value,
        channel = data["channel_id", true]?.snowflake?.let { VoiceChannel(it, value) },
        user = User(data["user_id"]!!.snowflake, value.client),
        sessionId = data["session_id"]!!.string,
        isDeafened = data["deaf"]!!.boolean,
        isMuted = data["mute"]!!.boolean,
        isSelfDeafened = data["self_deaf"]!!.boolean,
        isSelfMuted = data["self_mute"]!!.boolean,
        isSupressed = data["suppress"]!!.boolean,
        isStreaming = data["self_stream"]?.boolean ?: false,
        hasCameraEnabled = data["self_video"]!!.boolean,
        requestToSpeakTimestamp = data["request_to_speak_timestamp", true]?.isoTimestamp,
        member = data["member"]?.jsonObject?.let { Member(it, value) }
    )

}