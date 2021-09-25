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