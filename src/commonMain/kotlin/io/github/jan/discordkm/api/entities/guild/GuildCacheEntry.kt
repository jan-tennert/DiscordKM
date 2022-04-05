package io.github.jan.discordkm.api.entities.guild

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.DiscordLocale
import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannel
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannelImpl
import io.github.jan.discordkm.api.entities.channels.guild.GuildTextChannel
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannel
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.containers.CacheEmoteContainer
import io.github.jan.discordkm.api.entities.containers.CacheGuildChannelContainer
import io.github.jan.discordkm.api.entities.containers.CacheGuildMemberContainer
import io.github.jan.discordkm.api.entities.containers.CacheGuildRoleContainer
import io.github.jan.discordkm.api.entities.containers.CacheGuildThreadContainer
import io.github.jan.discordkm.api.entities.containers.CacheScheduledEventContainer
import io.github.jan.discordkm.api.entities.containers.CacheStickerContainer
import io.github.jan.discordkm.api.entities.containers.StickerContainer
import io.github.jan.discordkm.api.entities.guild.welcome.screen.WelcomeScreen
import io.github.jan.discordkm.api.events.GuildMembersChunkEvent
import io.github.jan.discordkm.internal.caching.CacheEntry
import io.github.jan.discordkm.internal.caching.GuildCacheManager
import io.github.jan.discordkm.internal.entities.DiscordImage
import io.github.jan.discordkm.internal.serialization.RequestGuildMemberPayload
import io.github.jan.discordkm.internal.utils.safeValues
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

interface GuildCacheEntry : Guild, Nameable, CacheEntry {
    val iconHash: String?
    val splashHash: String?
    val afkChannel: VoiceChannel?
    val afkTimeout: TimeSpan
    val verificationLevel: Guild.VerificationLevel
    val defaultMessageNotifications: Guild.NotificationLevel
    val explicitContentFilter: Guild.ExplicitContentFilter
    val features: Set<Guild.Feature>
    val mfaLevel: Guild.MfaLevel
    val applicationId: Snowflake?
    val widgetEnabled: Boolean
    val widgetChannel: GuildChannel? //TODO: Make channel ids actual channel but with optional caching
    val systemChannel: GuildTextChannel?
    val systemChannelFlags: Set<Guild.SystemChannelFlag>
    val rulesChannel: GuildTextChannel?
    val joinedAt: DateTimeTz?
    val isLarge: Boolean
    val isUnavailable: Boolean
    val memberCount: Int
    val vanityUrlCode: String?
    val description: String?
    val bannerHash: String?
    val premiumTier: Guild.PremiumTier
    val premiumSubscriptionCount: Int
    val preferredLocale: DiscordLocale
    val publicUpdatesChannel: GuildTextChannel?
    val owner: User
    val welcomeScreen: WelcomeScreen?
    val discoveryHash: String?
    val hasPremiumProgressBarEnabled: Boolean
    val nsfwLevel: Guild.NSFWLevel
    override val roles: CacheGuildRoleContainer
    override val members: CacheGuildMemberContainer
    override val threads: CacheGuildThreadContainer
    override val channels: CacheGuildChannelContainer
    override val stickers: StickerContainer
    val voiceStates: Map<Snowflake, VoiceStateCacheEntry>
    val presences: Map<Snowflake, Guild.GuildPresenceCacheEntry>
    val stageInstances: Map<Snowflake, StageInstanceCacheEntry>
    override val emotes: CacheEmoteContainer
    override val scheduledEvents: CacheScheduledEventContainer

    /**
     * The discovery image shown on the discovery tab
     */
    val discoveryImageUrl: String?

    /**
     * The icon of this guild
     */
    val iconUrl: String?

    /**
     * The banner of this guild
     */
    val bannerUrl: String?

    /**
     * The splash of this guild
     */
    val splashUrl: String?

    val publicRole: RoleCacheEntry

    val shardId get() = (if (client.config.totalShards != -1) (id.long shr 22) % client.config.totalShards else 0).toInt()

    /**
     * Requests the guild's members from the gateway and updates the guild's cache
     * @param query The member's username should start with
     * @param limit The maximum amount of members to request (required when using [query])
     * @param receivePresences Use this to also receive the members' presences
     * @param users The users you want to receive the member for. Can be left empty
     * @param timeout When something goes wrong, the function will not block forever and will just return an empty list when the timeout is reached
     * @return All members requested
     */
    suspend fun requestGuildMembers(
        query: String? = null,
        limit: Int = 0,
        receivePresences: Boolean = false,
        users: Collection<Snowflake> = emptyList(),
        timeout: TimeSpan? = null,
    ): List<MemberCacheEntry>

}

/**
 * A guild cache entry contains all information given by the Discord API
 * @param id The id of the guild
 * @param name The name of the guild
 * @param iconHash The icon hash of the guild
 * @param splashHash The splash hash of the guild
 * @param ownerId The id of the owner of the guild
 * @param afkChannelId The id of the afk channel
 * @param afkTimeout The afk timeout of the guild
 * @param verificationLevel The verification level of the guild
 * @param defaultMessageNotifications The default message notifications of the guild
 * @param explicitContentFilter The explicit content filter of the guild
 * @param features The features of the guild
 * @param mfaLevel The mfa level of the guild
 * @param applicationId The id of the application
 * @param widgetEnabled Whether the widget is enabled or not
 * @param widgetChannelId The id of the widget channel
 * @param systemChannelId The id of the system channel
 * @param memberCount The member count of the guild
 * @param joinedAt The time the user joined the guild
 * @param isLarge Whether the guild is large or not
 * @param isUnavailable Whether the guild is unavailable or not
 * @param memberCount The member count of the guild
 * @param vanityUrlCode The vanity url of the guild
 * @param description The description of the guild
 * @param bannerHash The banner hash of the guild
 * @param premiumTier The premium tier of the guild
 * @param premiumSubscriptionCount The premium subscription count of the guild
 * @param preferredLocale The preferred locale of the guild
 * @param publicUpdatesChannelId The id of the public updates channel
 * @param bannerHash The banner of the guild
 * @param premiumSubscriptionCount The premium subscription count of the guild
 * @param systemChannelFlags The system channel flags of the guild
 * @param rulesChannelId The id of the rules channel
 *
 * @see GuildChannel
 * @see Role
 * @see Member
 * @see Emoji.Emote
 * @see Guild.VerificationLevel
 * @see Guild.NotificationLevel
 * @see Guild.ExplicitContentFilter
 * @See Guild.SystemChannelFlag
 * @see Guild.Feature
 */
internal class GuildCacheEntryImpl(
    override val id: Snowflake,
    override val client: Client,
    override val name: String,
    override val iconHash: String?,
    override val splashHash: String?,
    afkChannelId: Snowflake?,
    override val afkTimeout: TimeSpan,
    override val verificationLevel: Guild.VerificationLevel,
    override val defaultMessageNotifications: Guild.NotificationLevel,
    override val explicitContentFilter: Guild.ExplicitContentFilter,
    override val features: Set<Guild.Feature>,
    override val mfaLevel: Guild.MfaLevel,
    override val applicationId: Snowflake?,
    override val widgetEnabled: Boolean,
    widgetChannelId: Snowflake?, //TODO: Make channel ids actual channel but with optional caching
    systemChannelId: Snowflake?,
    override val systemChannelFlags: Set<Guild.SystemChannelFlag>,
    rulesChannelId: Snowflake?,
    override val joinedAt: DateTimeTz?,
    override val isLarge: Boolean,
    override val isUnavailable: Boolean,
    override val memberCount: Int,
    override val vanityUrlCode: String?,
    override val description: String?,
    override val bannerHash: String?,
    override val premiumTier: Guild.PremiumTier,
    override val premiumSubscriptionCount: Int,
    override val preferredLocale: DiscordLocale,
    publicUpdatesChannelId: Snowflake?,
    ownerId: Snowflake,
    override val welcomeScreen: WelcomeScreen?,
    override val discoveryHash: String?,
    override val hasPremiumProgressBarEnabled: Boolean,
    override val nsfwLevel: Guild.NSFWLevel
) : GuildCacheEntry {

    val cacheManager = GuildCacheManager(this.client)

    override val roles: CacheGuildRoleContainer
        get() = CacheGuildRoleContainer(this, cacheManager.roleCache.safeValues)
    override val members: CacheGuildMemberContainer
        get() = CacheGuildMemberContainer(this, cacheManager.memberCache.safeValues)
    override val threads: CacheGuildThreadContainer
        get() = CacheGuildThreadContainer(this, cacheManager.threadCache.safeValues)
    override val channels: CacheGuildChannelContainer
        get() = CacheGuildChannelContainer(this, cacheManager.channelCache.safeValues)
    override val stickers: StickerContainer
        get() = CacheStickerContainer(this, cacheManager.stickerCache.safeValues)
    override val voiceStates: Map<Snowflake, VoiceStateCacheEntry>
        get() = cacheManager.voiceStates.safeValues.associateBy { it.user.id }
    override val presences: Map<Snowflake, Guild.GuildPresenceCacheEntry>
        get() = cacheManager.presences.safeValues.associateBy { it.user.id }
    override val stageInstances: Map<Snowflake, StageInstanceCacheEntry>
        get() = cacheManager.stageInstanceCache.safeValues.associateBy { it.id }
    override val emotes: CacheEmoteContainer
        get() = CacheEmoteContainer(this, cacheManager.emoteCache.safeValues)
    override val scheduledEvents: CacheScheduledEventContainer
        get() = CacheScheduledEventContainer(this, cacheManager.guildScheduledEventCache.safeValues)

    override val owner = User(ownerId, client)
    override val afkChannel = afkChannelId?.let { VoiceChannel(it, this) }
    override val publicUpdatesChannel = publicUpdatesChannelId?.let { GuildTextChannel(it, this) }
    override val widgetChannel: GuildChannel? = widgetChannelId?.let { GuildChannelImpl(id, this) }
    override val systemChannel = systemChannelId?.let { GuildTextChannel(it, this) }
    override val rulesChannel = rulesChannelId?.let { GuildTextChannel(it, this) }

    override val publicRole: RoleCacheEntry
        get() = cacheManager.roleCache.safeValues.first { it.id == id }

    /**
     * The discovery image shown on the discovery tab
     */
    override val discoveryImageUrl = discoveryHash?.let { DiscordImage.guildDiscoverySplash(id, it) }

    /**
     * The icon of this guild
     */
    override val iconUrl = iconHash?.let { DiscordImage.guildIcon(id, it) }

    /**
     * The banner of this guild
     */
    override val bannerUrl = bannerHash?.let { DiscordImage.guildBanner(id, it) }

    /**
     * The splash of this guild
     */
    override val splashUrl = splashHash?.let { DiscordImage.guildSplash(id, it) }

    override val shardId get() = (if (client.config.totalShards != -1) (id.long shr 22) % client.config.totalShards else 0).toInt()

    /**
     * Requests the guild's members from the gateway and updates the guild's cache
     * @param query The member's username should start with
     * @param limit The maximum amount of members to request (required when using [query])
     * @param receivePresences Use this to also receive the members' presences
     * @param users The users you want to receive the member for. Can be left empty
     */
    suspend fun requestGuildMembersAsync(
        query: String? = null,
        limit: Int = 0,
        receivePresences: Boolean = false,
        users: Collection<Snowflake> = emptyList(),
    ) {
        if (client is DiscordWebSocketClient) {
            val shard = client.shardById[shardId] ?: return
            shard.send(RequestGuildMemberPayload(id, query, limit, receivePresences, users))
        }
    }

    /**
     * Requests the guild's members from the gateway and updates the guild's cache
     * @param query The member's username should start with
     * @param limit The maximum amount of members to request (required when using [query])
     * @param receivePresences Use this to also receive the members' presences
     * @param users The users you want to receive the member for. Can be left empty
     * @param timeout When something goes wrong, the function will not block forever and will just return an empty list when the timeout is reached
     * @return All members requested
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun requestGuildMembers(
        query: String?,
        limit: Int,
        receivePresences: Boolean,
        users: Collection<Snowflake>,
        timeout: TimeSpan?
    ): List<MemberCacheEntry> {
        if (client !is DiscordWebSocketClient) return emptyList()
        val shard = client.shardById[shardId] ?: return emptyList()
        shard.send(RequestGuildMemberPayload(id, query, limit, receivePresences, users))

        suspend fun receiveMembers() = suspendCancellableCoroutine<List<MemberCacheEntry>> {
            val members = mutableListOf<MemberCacheEntry>()
            client.on<GuildMembersChunkEvent> {
                members.addAll(this.members)
                if (chunkCount == chunkIndex + 1) {
                    it.resume(members) { it.printStackTrace() }
                }
            }
        }

        return if (timeout != null) withTimeoutOrNull(timeout.millisecondsLong) { receiveMembers() }
            ?: emptyList() else receiveMembers()
    }

    override fun equals(other: Any?) = other is Guild && other.id == id
    override fun toString() = "GuildCacheEntry(id=$id, name=$name)"
    override fun hashCode() = id.hashCode()

}

internal val GuildCacheEntry.cacheManager: GuildCacheManager
    get() = (this as GuildCacheEntryImpl).cacheManager