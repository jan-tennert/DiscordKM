package io.github.jan.discordkm.api.entities.guild

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.ISO8601
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannel
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.buildRestAction
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Represents a member's voice state in a guild.
 *
 * @param guild The guild where this voice state is located.
 * @param channel The channel this member is connected to.
 * @param user The user this voice state is for.
 * @param sessionId The session ID of this voice state.
 * @param isDeafend Whether this member is deafened.
 * @param isMuted Whether this member is muted.
 * @param isSelfDeafend Whether this member is self-deafened.
 * @param isSelfMuted Whether this member is self-muted.
 * @param isStreaming Whether this member is streaming.
 * @param isSupressed Whether this member is suppressed.
 * @param hasCameraEnabled Whether this member has his camera enabled.
 * @param requestToSpeakTimestamp The timestamp of when this member requested to speak.
 */
class VoiceStateCacheEntry(
    override val guild: Guild,
    val channel: VoiceChannel?,
    val member: MemberCacheEntry?,
    val user: User,
    val sessionId: String,
    val isDeafened: Boolean,
    val isMuted: Boolean,
    val isSelfDeafened: Boolean,
    val isSelfMuted: Boolean,
    val isStreaming: Boolean,
    val isSupressed: Boolean,
    val hasCameraEnabled: Boolean,
    val requestToSpeakTimestamp: DateTimeTz?
) : GuildEntity {

    /**
     * Whether the user is in a voice channel.
     */
    val isInVoiceChannel: Boolean
        get() = channel != null

    /**
     * Whether this user is the bot
     */
    val isSelfUser: Boolean
        get() = client.selfUser.id == user.id

    /**
     * Invites this user to speak in stage channel
     */
    suspend fun inviteToSpeak() = client.buildRestAction<Unit> {
        route = Route.Voice.MODIFY_VOICE_STATE(guild.id, user.id).patch(buildJsonObject {
            put("channel_id", channel?.id?.string)
            put("suppress", false)
            put("request_to_speak_timestamp", ISO8601.DATETIME_UTC_COMPLETE.format(DateTimeTz.nowLocal()))
        })
        transform {  }
    }

    /**
     * Accepts his speak request
     */
    suspend fun acceptSpeakRequest() = client.buildRestAction<Unit> {
        route = Route.Voice.MODIFY_VOICE_STATE(guild.id, if(isSelfUser) "@me" else user.id).patch(buildJsonObject {
            put("channel_id", channel?.id?.string)
            put("suppress", false)
        })
        transform {  }
    }

    /**
     * Declines his speak request
     */
    suspend fun declineSpeakRequest() = client.buildRestAction<Unit> {
        route = Route.Voice.MODIFY_VOICE_STATE(guild.id, if(isSelfUser) "@me" else user.id).patch(buildJsonObject {
            put("channel_id", channel?.id?.string)
            put("suppress", true)
        })
        transform {  }
    }

}