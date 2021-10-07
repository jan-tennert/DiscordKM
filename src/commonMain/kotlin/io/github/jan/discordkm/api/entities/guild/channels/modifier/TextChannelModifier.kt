/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild.channels.modifier

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.channels.GuildTextChannel
import io.github.jan.discordkm.api.entities.guild.channels.PermissionOverride
import io.github.jan.discordkm.api.entities.guild.channels.Thread
import io.github.jan.discordkm.internal.check
import io.github.jan.discordkm.internal.utils.checkAndReturn
import io.github.jan.discordkm.internal.utils.putJsonObject
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.buildJsonObject

class TextChannelModifier(private val type: Int?) : NonCategoryModifier<GuildTextChannel> {

    override var name: String? = null

    /**
     * Whether this channel should be marked as a NSFWChannel.
     */
    var nsfw: Boolean? = null

    /**
     * Sets the new topic for the text channel
     */
    var topic: String? = null

    /**
     * Sets the new slow mode time for the text channel.
     * 0-21600 seconds
     */
    var slowModeTime: TimeSpan? = null
    var defaultAutoArchiveDuration: Thread.ThreadDuration? = null

    override var parentId: Snowflake? = null
    override var position: Int? = null
    override var permissionOverrides: MutableList<PermissionOverride> = mutableListOf<PermissionOverride>()

    override fun build() = checkAndReturn {
        slowModeTime.check("The slowmode time has to be between zero and 21600 seconds") { it.seconds < 21600 && it.seconds > 0 }

        buildJsonObject {
            putOptional("default_auto_archive_duration", defaultAutoArchiveDuration?.duration?.minutes?.toInt())
            putOptional("rate_limit_per_user", slowModeTime?.seconds?.toInt())
            putOptional("topic", topic)
            putOptional("nsfw", nsfw)
            putOptional("type", type)
            putJsonObject(super.build())
        }
    }

}