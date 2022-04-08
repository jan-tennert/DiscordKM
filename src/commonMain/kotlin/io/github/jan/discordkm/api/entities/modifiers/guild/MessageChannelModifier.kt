/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.modifiers.guild

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.api.entities.modifiers.JsonModifier
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

sealed interface MessageChannelModifier : JsonModifier {

    val isThread: Boolean

    var slowModeTime: TimeSpan?

    /*
     * After this time this [thread] gets automatically achieved
     */
    var autoArchiveDuration: Thread.ThreadDuration?

    override val data: JsonObject
        get() = buildJsonObject {
            putOptional("rate_limit_per_user", slowModeTime?.seconds?.toInt())
            putOptional("${if(isThread) "" else "default_"}auto_archive_duration", autoArchiveDuration?.duration?.minutes?.toInt())
        }

}