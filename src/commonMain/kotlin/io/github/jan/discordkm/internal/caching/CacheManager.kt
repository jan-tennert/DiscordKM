/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.caching

import co.touchlab.stately.collections.IsoMutableMap
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.ThreadCacheEntry
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.GuildCacheEntry
import io.github.jan.discordkm.api.entities.guild.VoiceStateCacheEntry
import io.github.jan.discordkm.api.entities.guild.member.MemberCacheEntry
import io.github.jan.discordkm.api.entities.guild.role.Role
import io.github.jan.discordkm.api.entities.guild.role.RoleCacheEntry
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEventCacheEntry
import io.github.jan.discordkm.api.entities.guild.stage.StageInstanceCacheEntry
import io.github.jan.discordkm.api.entities.guild.sticker.Sticker
import io.github.jan.discordkm.api.entities.message.MessageCacheEntry
import io.github.jan.discordkm.api.entities.message.MessageReaction

sealed class CacheManager <T : CacheManager<T>>{
    private val caches = mutableListOf<IsoMutableMap<*, *>>()
    abstract val client: DiscordClient

    fun <K, V> createCache(flag: CacheFlag) : Cache<V> {
        val cache = Cache<V>(flag, client)
        caches.add(cache)
        return cache
    }

    fun clearAll() = caches.forEach { it.clear() }
    abstract fun fillCache(cache: T)
}

class ClientCacheManager internal constructor(override val client: DiscordClient) : CacheManager<ClientCacheManager>() {

    val guildCache = createCache<Snowflake, GuildCacheEntry>(CacheFlag.GUILDS)

    override fun fillCache(cache: ClientCacheManager) = cache.guildCache.putAll(guildCache)
}

class GuildCacheManager internal constructor(override val client: DiscordClient) : CacheManager<GuildCacheManager>() {
    val memberCache = createCache<Snowflake, MemberCacheEntry>(CacheFlag.MEMBERS)
    val presences = createCache<Snowflake, Guild.GuildPresenceCacheEntry>(CacheFlag.PRESENCES)
    val voiceStates = createCache<Snowflake, VoiceStateCacheEntry>(CacheFlag.VOICE_STATES)
    val channelCache = createCache<Snowflake, GuildChannelCacheEntry>(CacheFlag.CHANNELS)
    val threadCache = createCache<Snowflake, ThreadCacheEntry>(CacheFlag.THREADS)
    val roleCache = createCache<Snowflake, RoleCacheEntry>(CacheFlag.ROLES)
    val emoteCache = createCache<Snowflake, Emoji.Emote>(CacheFlag.EMOJIS)
    val stickerCache = createCache<Snowflake, Sticker>(CacheFlag.STICKERS)
    val stageInstanceCache = createCache<Snowflake, StageInstanceCacheEntry>(CacheFlag.STAGE_INSTANCES)
    val guildScheduledEventCache = createCache<Snowflake, ScheduledEventCacheEntry>(CacheFlag.SCHEDULED_EVENTS)

    override fun fillCache(cache: GuildCacheManager) = cache.let {
        it.memberCache.putAll(memberCache)
        it.presences.putAll(presences)
        it.voiceStates.putAll(voiceStates)
        it.channelCache.putAll(channelCache)
        it.threadCache.putAll(threadCache)
        it.roleCache.putAll(roleCache)
        it.emoteCache.putAll(emoteCache)
        it.stickerCache.putAll(stickerCache)
    }
}

class MemberCacheManager(override val client: DiscordClient) : CacheManager<MemberCacheManager>() {
    val roleCache = createCache<Snowflake, Role>(CacheFlag.ROLES)

    override fun fillCache(cache: MemberCacheManager) = cache.roleCache.putAll(roleCache)
}

class MessageCacheManager(override val client: DiscordClient) : CacheManager<MessageCacheManager>() {
    val messageCache = createCache<Snowflake, MessageCacheEntry>(CacheFlag.MESSAGES)

    override fun fillCache(cache: MessageCacheManager) = cache.messageCache.putAll(messageCache)
}

class ReactionCacheManager(override val client: DiscordClient) : CacheManager<ReactionCacheManager>() {
    val reactionCache = createCache<Snowflake, MessageReaction>(CacheFlag.REACTIONS)

    override fun fillCache(cache: ReactionCacheManager) = cache.reactionCache.putAll(reactionCache)
}