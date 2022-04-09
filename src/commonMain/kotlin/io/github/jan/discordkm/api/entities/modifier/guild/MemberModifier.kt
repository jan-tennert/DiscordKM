/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.modifier.guild

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.ISO8601
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannel
import io.github.jan.discordkm.api.entities.guild.role.Role
import io.github.jan.discordkm.api.entities.modifier.JsonModifier
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.buildJsonObject

class MemberModifier : JsonModifier {

    /*
     * The new nickname of the member
     */
    var nickname: String? = null

    /*
     * A list of role ids the member will get
     */
    val roles = mutableListOf<Snowflake>()

    /*
     * Whether the member should be muted
     */
    var mute: Boolean? = null

    /*
     * Whether the member should be deafend
     */
    var deaf: Boolean? = null

    var timeoutUntil: DateTimeTz? = null
    private var channelId: Snowflake? = null

    /*
     * Adds a role to the member
     */
    fun role(role: Role) {
        roles += role.id
    }

    /*
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
