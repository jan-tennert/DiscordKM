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
import io.github.jan.discordkm.api.entities.guild.channels.Thread
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.buildJsonObject

class ThreadModifier(val thread: Thread) {

    var name: String? = null
    var slowModeTime: TimeSpan? = null
    var autoArchiveDuration: Thread.ThreadDuration? = null
    var achiev: Boolean? = null
    var lock: Boolean? = null

    fun build() = buildJsonObject {
        putOptional("name", name)
        putOptional("rate_limit_per_user", slowModeTime?.seconds?.toInt())
        putOptional("auto_achieve_duration", autoArchiveDuration?.duration?.minutes?.toInt())
        putOptional("achieved", achiev)
        putOptional("locked", lock)
    }

}