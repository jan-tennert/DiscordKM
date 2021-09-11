package io.github.jan.discordkm.entities.guild.channels.modifier

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.guild.channels.GuildTextChannel
import io.github.jan.discordkm.entities.guild.channels.PermissionOverride
import io.github.jan.discordkm.entities.guild.channels.Thread
import io.github.jan.discordkm.utils.putJsonObject
import io.github.jan.discordkm.utils.putOptional
import kotlinx.serialization.json.buildJsonObject

class TextChannelModifier(private val type: Int?) : NonCategoryModifier<GuildTextChannel> {

    override var name: String? = null
    var nsfw: Boolean? = null
    var topic: String? = null
    var slowModeTime: TimeSpan? = null
    var defaultAutoArchiveDuration: Thread.ThreadDuration? = null
    override var parentId: Snowflake? = null
    override var position: Int? = null
    override var permissionOverrides: MutableList<PermissionOverride> = mutableListOf<PermissionOverride>()

    override fun build() = buildJsonObject {
        putOptional("default_auto_archive_duration", defaultAutoArchiveDuration?.duration?.minutes?.toInt())
        putOptional("rate_limit_per_user", slowModeTime?.seconds?.toInt())
        putOptional("topic", topic)
        putOptional("nsfw", nsfw)
        putOptional("type", type)
        putJsonObject(super.build())
    }

}
