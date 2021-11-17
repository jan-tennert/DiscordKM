package io.github.jan.discordkm.internal.caching

import co.touchlab.stately.collections.IsoMutableMap
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.UserCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.ThreadCacheEntry
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.GuildCacheEntry
import io.github.jan.discordkm.api.entities.guild.MemberCacheEntry
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.api.entities.guild.RoleCacheEntry
import io.github.jan.discordkm.api.entities.guild.StageInstanceCacheEntry
import io.github.jan.discordkm.api.entities.guild.Sticker
import io.github.jan.discordkm.api.entities.guild.VoiceStateCacheEntry
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEventCacheEntry
import io.github.jan.discordkm.api.entities.messages.MessageCacheEntry
import io.github.jan.discordkm.api.entities.messages.MessageReaction

sealed class CacheManager <T : CacheManager<T>>{
    private val caches = mutableListOf<IsoMutableMap<*, *>>()
    abstract val client: Client

    fun <K, V> createCache(flag: CacheFlag) : Cache<K, V> {
        val cache = Cache<K, V>(flag, client)
        caches.add(cache)
        return cache
    }

    fun clearAll() = caches.forEach { it.clear() }
    abstract fun fillCache(cache: T)
}

class ClientCacheManager internal constructor(override val client: Client) : CacheManager<ClientCacheManager>() {

    val guildCache = createCache<Snowflake, GuildCacheEntry>(CacheFlag.GUILDS)
    val userCache = createCache<Snowflake, UserCacheEntry>(CacheFlag.USERS)

    override fun fillCache(cache: ClientCacheManager) = cache.let {
        it.guildCache.putAll(guildCache)
        it.userCache.putAll(userCache)
    }
}

class GuildCacheManager internal constructor(override val client: Client) : CacheManager<GuildCacheManager>() {
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

class MemberCacheManager(override val client: Client) : CacheManager<MemberCacheManager>() {
    val roleCache = createCache<Snowflake, Role>(CacheFlag.ROLES)

    override fun fillCache(cache: MemberCacheManager) = cache.roleCache.putAll(roleCache)
}

class MessageCacheManager(override val client: Client) : CacheManager<MessageCacheManager>() {
    val messageCache = createCache<Snowflake, MessageCacheEntry>(CacheFlag.MESSAGES)

    override fun fillCache(cache: MessageCacheManager) = cache.messageCache.putAll(messageCache)
}

class ReactionCacheManager(override val client: Client) : CacheManager<ReactionCacheManager>() {
    val reactionCache = createCache<Snowflake, MessageReaction>(CacheFlag.REACTIONS)

    override fun fillCache(cache: ReactionCacheManager) = cache.reactionCache.putAll(reactionCache)
}