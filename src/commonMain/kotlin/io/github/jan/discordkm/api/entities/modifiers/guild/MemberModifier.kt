package io.github.jan.discordkm.api.entities.modifiers.guild

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.ISO8601
import io.github.jan.discordkm.api.entities.Modifier
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannel
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.api.entities.modifiers.BaseModifier
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.buildJsonObject

class MemberModifier : BaseModifier {

    /**
     * The new nickname of the member
     */
    var nickname: String? = null

    /**
     * A list of role ids the member will get
     */
    val roles = mutableListOf<Snowflake>()

    /**
     * Whether the member should be muted
     */
    var mute: Boolean? = null

    /**
     * Whether the member should be deafend
     */
    var deaf: Boolean? = null

    var timeoutUntil: DateTimeTz? = null
    private var channelId: Snowflake? = null

    /**
     * Adds a role to the member
     */
    fun role(role: Role) {
        roles += role.id
    }

    /**
     * Moves the member to a voice channel
     */
    fun moveTo(voiceChannel: VoiceChannel) {
        channelId = voiceChannel.id
    }

    override val data get() = buildJsonObject {
        putOptional("nickname", nickname)
        putOptional("roles", roles.ifEmpty { null })
        putOptional("mute", mute)
        putOptional("deaf", deaf)
        putOptional("channel_id", channelId)
        putOptional("communication_disabled_until", timeoutUntil?.let { ISO8601.DATETIME_UTC_COMPLETE.format(it) })
    }

}
