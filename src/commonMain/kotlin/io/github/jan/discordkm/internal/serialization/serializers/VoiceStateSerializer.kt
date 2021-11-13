package io.github.jan.discordkm.internal.serialization.serializers

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannel
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.api.entities.guild.VoiceStateCacheEntry
import io.github.jan.discordkm.internal.serialization.GuildEntitySerializer
import io.github.jan.discordkm.internal.utils.boolean
import io.github.jan.discordkm.internal.utils.isoTimestamp
import io.github.jan.discordkm.internal.utils.snowflake
import io.github.jan.discordkm.internal.utils.string
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

object VoiceStateSerializer : GuildEntitySerializer<VoiceStateCacheEntry> {

    override fun deserialize(data: JsonObject, value: Guild) = VoiceStateCacheEntry(
        guild = value,
        channel = data["channel_id"]?.snowflake?.let { VoiceChannel.Companion.from(it, value) },
        user = User.from(data["user_id"]!!.snowflake, value.client),
        sessionId = data["session_id"]!!.string,
        isDeafened = data["deaf"]!!.boolean,
        isMuted = data["mute"]!!.boolean,
        isSelfDeafened = data["self_deaf"]!!.boolean,
        isSelfMuted = data["self_mute"]!!.boolean,
        isSupressed = data["suppress"]!!.boolean,
        isStreaming = data["self_stream"]!!.boolean,
        hasCameraEnabled = data["self_video"]!!.boolean,
        requestToSpeakTimestamp = data["request_to_speak_timestamp"]?.isoTimestamp,
        member = Member.from(data["member"]!!.jsonObject, value)
    )

}