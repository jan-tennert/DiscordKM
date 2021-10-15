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
import io.github.jan.discordkm.api.entities.Modifier
import io.github.jan.discordkm.api.entities.guild.channels.Thread
import io.github.jan.discordkm.internal.Check
import io.github.jan.discordkm.internal.check
import io.github.jan.discordkm.internal.checkRange
import io.github.jan.discordkm.internal.utils.checkAndReturn
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.buildJsonObject

class ThreadModifier(val thread: Thread) : Modifier {

    var name: String? = null
    var slowModeTime: TimeSpan? = null
        set(value) {
            value?.seconds?.toInt().checkRange("slow mode time", Check.SLOWMODE)
            field = value
        }

    /**
     * After this time this [thread] gets automatically achieved
     */
    var autoArchiveDuration: Thread.ThreadDuration? = null

    /**
     * Whether this thread should be archived
     */
    var archive: Boolean? = null

    /**
     * Whether this thread should be locked. Means normal users can't unarchive the thread
     */
    var lock: Boolean? = null

    override fun build() = checkAndReturn {
        slowModeTime.check("The slowmode time has to be between zero and 21600 seconds") { it.seconds < 21600 && it.seconds > 0 }

        buildJsonObject {
            putOptional("name", name)
            putOptional("rate_limit_per_user", slowModeTime?.seconds?.toInt())
            putOptional("auto_achieve_duration", autoArchiveDuration?.duration?.minutes?.toInt())
            putOptional("achieved", archive)
            putOptional("locked", lock)
        }
    }

}