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