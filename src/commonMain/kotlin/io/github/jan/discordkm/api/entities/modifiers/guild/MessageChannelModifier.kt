package io.github.jan.discordkm.api.entities.modifiers.guild

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.api.entities.modifiers.BaseModifier
import io.github.jan.discordkm.internal.Check
import io.github.jan.discordkm.internal.checkRange
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

sealed interface MessageChannelModifier : BaseModifier {

    val isThread: Boolean

    var slowModeTime: TimeSpan?

    /**
     * After this time this [thread] gets automatically achieved
     */
    var autoArchiveDuration: Thread.ThreadDuration?

    override val data: JsonObject
        get() = buildJsonObject {
            putOptional("rate_limit_per_user", slowModeTime?.seconds?.toInt())
            putOptional("${if(isThread) "" else "default_"}auto_archive_duration", autoArchiveDuration?.duration?.minutes?.toInt())
        }

}