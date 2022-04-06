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

/**
 * A guild cache entry contains all information given by the Discord API
**/
interface GuildCacheEntry : Guild, Nameable, CacheEntry {

    /**
     * The icon hash of the guild
     */
    val iconHash: String?

    /**
     * The splash hash of the guild
     */
    val splashHash: String?

    /**
     * The channel where members get moved to when they are longer afk than [afkTimeout]
     */
    val afkChannel: VoiceChannel?

    /**
     * The timeout after an afk member gets moved to [afkChannel]
     */
    val afkTimeout: TimeSpan

    /**
     * The verification level of the guild
     */
    val verificationLevel: Guild.VerificationLevel

    /**
     * The default message notifications of the guild
     */
    val defaultMessageNotifications: Guild.NotificationLevel

    /**
     * The explicit content filter of the guild
     */
    val explicitContentFilter: Guild.ExplicitContentFilter

    /**
     * The features of the guild
     */
    val features: Set<String>

    /**
     * The mfa level of the guild
     */
    val mfaLevel: Guild.MfaLevel

    /**
     * The id of the application that created the guild, if it was created by an application
     */
    val applicationId: Snowflake?

    /**
     * Whether widgets are enabled, or not
     */
    val widgetEnabled: Boolean

    /**
     * The channel the widget referred to
     */
    val widgetChannel: GuildChannel?

    /**
     * The channel the system messages are sent to
     */
    val systemChannel: GuildTextChannel?

    /**
     * The flags of the [systemChannel]
     */
    val systemChannelFlags: Set<Guild.SystemChannelFlag>

    /**
     * The channel where the rules are posted
     */
    val rulesChannel: GuildTextChannel?

    /**
     * The time the user joined the guild
     */
    val joinedAt: DateTimeTz?

    /**
     * Whether the guild is considered as large or not
     */
    val isLarge: Boolean

    /**
     * Whether the guild is unavailable or not
     */
    val isUnavailable: Boolean

    /**
     * The member count of the guild
     */
    val memberCount: Int

    /**
     * The vanity url code of the guild
     */
    val vanityUrlCode: String?

    /**
     * The description of the guild
     */
    val description: String?

    /**
     * The banner hash of the guild
     */
    val bannerHash: String?

    /**
     * The boost level of the guild
     */
    val premiumTier: Guild.PremiumTier

    /**
     * The amount of people boosting the guild
     */
    val premiumSubscriptionCount: Int

    /**
     * The preferred locale of the guild
     */
    val preferredLocale: DiscordLocale

    /**
     * The public updates channel of the guild
     */
    val publicUpdatesChannel: GuildTextChannel?

    /**
     * The owner of the guild
     */
    val owner: User

    /**
     * The screen shown when a new member joins the guild
     */
    val welcomeScreen: WelcomeScreen?

    /**
     * The discovery hash of the guild
     */
    val discoveryHash: String?

    /**
     * Whether the guild enabled premium progress bar
     */
    val hasPremiumProgressBarEnabled: Boolean

    /**
     * The NSFW Level of the guild
     */
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
     * The @everyone role of this guild
     */
    val publicRole: RoleCacheEntry
        get() = cacheManager.roleCache.safeValues.first { it.id == id }

    /**
     * The discovery image shown on the discovery tab
     */
    val discoveryImageUrl get() = discoveryHash?.let { DiscordImage.guildDiscoverySplash(id, it) }

    /**
     * The icon of this guild
     */
    val iconUrl get() = iconHash?.let { DiscordImage.guildIcon(id, it) }

    /**
     * The banner of this guild
     */
    val bannerUrl get()  = bannerHash?.let { DiscordImage.guildBanner(id, it) }

    /**
     * The splash of this guild
     */
    val splashUrl get() = splashHash?.let { DiscordImage.guildSplash(id, it) }

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

    operator fun contains(member: Member) = members.any { it.id == member.id }

    operator fun contains(user: User) = members.any { it.id == user.id }

    operator fun contains(role: Role) = roles.any { it.id == role.id }

    operator fun contains(channel: GuildChannel) = channels.any { it.id == channel.id }

}

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
    override val features: Set<String>,
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