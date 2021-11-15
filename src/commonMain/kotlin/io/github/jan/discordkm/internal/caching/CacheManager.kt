package io.github.jan.discordkm.internal.caching

import co.touchlab.stately.collections.IsoMutableMap
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.UserCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.ThreadCacheEntry
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.GuildCacheEntry
import io.github.jan.discordkm.api.entities.guild.MemberCacheEntry
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.api.entities.guild.RoleCacheEntry
import io.github.jan.discordkm.api.entities.guild.StageInstanceCacheEntry
import io.github.jan.discordkm.api.entities.guild.Sticker
import io.github.jan.discordkm.api.entities.guild.VoiceStateCacheEntry
import io.github.jan.discordkm.api.entities.messages.MessageCacheEntry
import io.github.jan.discordkm.api.entities.messages.MessageReaction

sealed class CacheManager <T : CacheManager<T>>{
    private val caches = mutableListOf<IsoMutableMap<*, *>>()

    fun <K, V> createCache() : IsoMutableMap<K, V> {
        val cache = IsoMutableMap<K, V>()
        caches.add(cache)
        return cache
    }

    fun clearAll() = caches.forEach { it.clear() }
    abstract fun fillCache(cache: T)
}

class ClientCacheManager internal constructor() : CacheManager<ClientCacheManager>() {

    val guildCache = createCache<Snowflake, GuildCacheEntry>()
    val userCache = createCache<Snowflake, UserCacheEntry>()

    override fun fillCache(cache: ClientCacheManager) = cache.let {
        it.guildCache.putAll(guildCache)
        it.userCache.putAll(userCache)
    }
}

class GuildCacheManager internal constructor() : CacheManager<GuildCacheManager>() {
    val memberCache = createCache<Snowflake, MemberCacheEntry>()
    val presences = createCache<Snowflake, Guild.GuildPresenceCacheEntry>()
    val voiceStates = createCache<Snowflake, VoiceStateCacheEntry>()
    val channelCache = createCache<Snowflake, GuildChannelCacheEntry>()
    val threadCache = createCache<Snowflake, ThreadCacheEntry>()
    val roleCache = createCache<Snowflake, RoleCacheEntry>()
    val emoteCache = createCache<Snowflake, Emoji.Emote>()
    val stickerCache = createCache<Snowflake, Sticker>()
    val stageInstanceCache = createCache<Snowflake, StageInstanceCacheEntry>()

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

class MemberCacheManager : CacheManager<MemberCacheManager>() {
    val roleCache = createCache<Snowflake, Role>()

    override fun fillCache(cache: MemberCacheManager) = cache.roleCache.putAll(roleCache)
}

class MessageCacheManager : CacheManager<MessageCacheManager>() {
    val messageCache = createCache<Snowflake, MessageCacheEntry>()

    override fun fillCache(cache: MessageCacheManager) = cache.messageCache.putAll(messageCache)
}

class ReactionCacheManager : CacheManager<ReactionCacheManager>() {
    val reactionCache = createCache<Snowflake, MessageReaction>()

    override fun fillCache(cache: ReactionCacheManager) = cache.reactionCache.putAll(reactionCache)
}