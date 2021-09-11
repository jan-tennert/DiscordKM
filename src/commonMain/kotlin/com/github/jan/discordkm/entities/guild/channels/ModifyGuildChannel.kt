package com.github.jan.discordkm.entities.guild.channels

import com.soywiz.klock.TimeSpan
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

interface GuildChannelModifier <C : GuildChannel> {
    var name: String?
    val originalType: Int
}

@Serializable
data class TextChannelModifier(override var name: String? = null, @Transient override val originalType: Int = -1, internal var type: Int = originalType, var nsfw: Boolean? = null, var topic: String? = null, internal var slowModeTime: Long? = null) : GuildChannelModifier<GuildTextChannel> {

    fun convertToNewsChannel() { if(originalType == 0) type = 5 }

    fun convertToTextChannel() { if(originalType == 5) type = 0 }

    fun setSlowmode(timeSpan: TimeSpan) { slowModeTime = timeSpan.seconds.toLong() }

}