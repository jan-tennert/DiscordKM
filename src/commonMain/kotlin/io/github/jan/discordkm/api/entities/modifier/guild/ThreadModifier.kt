/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.modifier.guild

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.internal.utils.modify
import io.github.jan.discordkm.internal.utils.putJsonObject
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.JsonObject

class ThreadModifier() : ParentalModifier(), MessageChannelModifier {

    override val isThread: Boolean
        get() = true

    override var autoArchiveDuration: Thread.ThreadDuration? = null
    override var slowModeTime: TimeSpan? = null

    /*
     * Whether this thread should be archived
     */
    var archive: Boolean? = null

    /*
     * Whether this thread should be locked. Means normal users can't unarchive the thread
     */
    var lock: Boolean? = null

    override val data: JsonObject
        get() = super<MessageChannelModifier>.data.modify {
            putOptional("name", name)
            putOptional("rate_limit_per_user", slowModeTime?.seconds?.toInt())
            putOptional("auto_archive_duration", autoArchiveDuration?.duration?.minutes?.toInt())
            putOptional("achieved", archive)
            putOptional("locked", lock)
            putJsonObject(super<ParentalModifier>.data)
        }

}