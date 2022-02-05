package io.github.jan.discordkm.api.entities.modifiers.guild

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.internal.check
import io.github.jan.discordkm.internal.utils.modify
import io.github.jan.discordkm.internal.utils.putJsonObject
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.JsonObject

class ThreadModifier() : ParentalModifier(), MessageChannelModifier {

    override val isThread: Boolean
        get() = true

    override var autoArchiveDuration: Thread.ThreadDuration? = null
    override var slowModeTime: TimeSpan? = null

    /**
     * Whether this thread should be archived
     */
    var archive: Boolean? = null

    /**
     * Whether this thread should be locked. Means normal users can't unarchive the thread
     */
    var lock: Boolean? = null

    override val data: JsonObject
        get() = super<MessageChannelModifier>.data.modify {
            slowModeTime.check("The slowmode time has to be between zero and 21600 seconds") { it.seconds < 21600 && it.seconds > 0 }
            putOptional("name", name)
            putOptional("rate_limit_per_user", slowModeTime?.seconds?.toInt())
            putOptional("auto_archive_duration", autoArchiveDuration?.duration?.minutes?.toInt())
            putOptional("achieved", archive)
            putOptional("locked", lock)
            putJsonObject(super<ParentalModifier>.data)
        }

}