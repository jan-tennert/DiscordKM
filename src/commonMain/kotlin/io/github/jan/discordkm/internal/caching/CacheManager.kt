package io.github.jan.discordkm.internal.caching

import co.touchlab.stately.collections.IsoMutableMap
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.UserCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.ThreadCacheEntry
import io.github.jan.discordkm.api.entities.guild.GuildCacheEntry
import io.github.jan.discordkm.api.entities.guild.GuildEntity
import io.github.jan.discordkm.api.entities.guild.MemberCacheEntry
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.api.entities.guild.RoleCacheEntry
import io.github.jan.discordkm.api.entities.messages.Message

sealed class CacheManager {
    private val caches = mutableListOf<IsoMutableMap<*, *>>()

    fun <K, V> createCache() : IsoMutableMap<K, V> {
        val cache = IsoMutableMap<K, V>()
        caches.add(cache)
        return cache
    }

    fun clearAll() = caches.forEach { it.clear() }
}

class ClientCacheManager internal constructor() : CacheManager() {

    val guildCache = createCache<Snowflake, GuildCacheEntry>()
    val userCache = createCache<Snowflake, UserCacheEntry>()

}

class GuildCacheManager internal constructor() : CacheManager() {
    val memberCache = createCache<Snowflake, MemberCacheEntry>()
    val activities = createCache<Snowflake, ActivityCacheEntry>()
    val voiceStates = createCache<Snowflake, VoiceStateCacheEntry>()
    val channelCache = createCache<Snowflake, ChannelCacheEntry>()
    val threadCache = createCache<Snowflake, ThreadCacheEntry>()
    val roleCache = createCache<Snowflake, RoleCacheEntry>()
}

class MemberCacheManager : CacheManager() {
    val roleCache = createCache<Snowflake, Role>()
}

class MessageCacheManager : CacheManager() {
    val messageCache = createCache<Snowflake, Message>()
}