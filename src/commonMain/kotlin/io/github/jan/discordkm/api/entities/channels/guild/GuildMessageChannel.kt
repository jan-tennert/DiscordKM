package io.github.jan.discordkm.api.entities.channels.guild

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.channels.MessageChannelCacheEntry

sealed interface GuildMessageChannel : GuildChannel, MessageChannel

sealed interface GuildMessageChannelCacheEntry : GuildMessageChannel, GuildChannelCacheEntry, MessageChannelCacheEntry, ParentChannel {

    val slowModeTime: TimeSpan

}