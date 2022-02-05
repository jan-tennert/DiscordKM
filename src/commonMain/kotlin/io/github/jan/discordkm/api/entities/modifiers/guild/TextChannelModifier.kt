package io.github.jan.discordkm.api.entities.modifiers.guild

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.internal.utils.modify
import io.github.jan.discordkm.internal.utils.putJsonObject
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.JsonObject

class TextChannelModifier : ParentalModifier(), MessageChannelModifier {

    /**
     * Whether this channel should be marked as a NSFWChannel.
     */
    var nsfw: Boolean? = null

    /**
     * Sets the new topic for the text channel
     */
    var topic: String? = null

    private var type: ChannelType? = null

    override val isThread: Boolean
        get() = false
    override var slowModeTime: TimeSpan? = null
    override var autoArchiveDuration: Thread.ThreadDuration? = null

    override val data: JsonObject
        get() = super<MessageChannelModifier>.data.modify {
            putOptional("default_auto_archive_duration", autoArchiveDuration?.duration?.minutes?.toInt())
            putOptional("rate_limit_per_user", slowModeTime?.seconds?.toInt())
            putOptional("topic", topic)
            putOptional("nsfw", nsfw)
            putOptional("type", type?.value)
            putJsonObject(super<ParentalModifier>.data)
        }

    fun convertToNewsChannel() { type = ChannelType.GUILD_NEWS }
    fun convertToTextChannel() { type = ChannelType.GUILD_TEXT }

}