/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.guild.channels

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.minutes
import kotlin.jvm.JvmInline

class Thread {

    /**
     * Represents the time where a thread is getting archived
     */
    @JvmInline
    value class ThreadDuration internal constructor(val duration: TimeSpan) {

        companion object {
            val HOUR = ThreadDuration(60.minutes)
            val DAY = ThreadDuration(1440.minutes)
            val THREE_DAYS = ThreadDuration(4320.minutes)
            val WEEK = ThreadDuration(10080.minutes)
            val ZERO = ThreadDuration(0.minutes)

            internal fun raw(duration: TimeSpan) = ThreadDuration(duration)
        }

    }

}