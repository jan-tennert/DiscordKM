package io.github.jan.discordkm.api.entities

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.misc.Color
import io.github.jan.discordkm.internal.caching.CacheEntry
import io.github.jan.discordkm.internal.entities.DiscordImage

/**
 * The user cache entry contains all information given by the Discord API
 */
sealed interface UserCacheEntry : User, CacheEntry, Nameable  {

    /**
     * The discriminator of the user.
     */
    val discriminator: String

    /**
     * Whether this user is a bot
     */
    val isBot: Boolean

    /**
     * Whether this is a system account
     */
    val isSystem: Boolean

    /**
     * Whether the user has 2FA enabled
     */
    val hasMfaEnabled: Boolean

    /**
     * The flags of the user's account
     */
    val flags: Set<User.UserFlag>

    /**
     * The type of nitro subscription the user has
     */
    val premiumType: User.PremiumType

    /**
     * The public flags on the user's account
     */
    val publicFlags: Set<User.UserFlag>

    /**
     * Represents the user profile containing the banner etc.
     */
    val profile: UserProfile

    /**
     * Whether the user doesn't have an own avatar and uses a default one
     */
    val usesDefaultAvatar: Boolean

}

internal class UserCacheEntryImpl(
    override val id : Snowflake,
    override val name: String,
    override val discriminator: String,
    avatarHash: String?,
    override val isBot: Boolean,
    override val isSystem: Boolean,
    override val hasMfaEnabled: Boolean,
    override val flags: Set<User.UserFlag>,
    override val premiumType: User.PremiumType,
    override val publicFlags: Set<User.UserFlag>,
    bannerHash: String?,
    accentColor: Color?,
    override val client: Client
) : UserCacheEntry {

    /**
     * The avatar url of the user
     */
    private val avatarUrl = avatarHash?.let { DiscordImage.userAvatar(id, it) } ?: DiscordImage.defaultUserAvatar(discriminator.toInt())

    /**
     * The banner url of the user
     */
    private val bannerUrl = bannerHash?.let { DiscordImage.userBanner(id, it) }

    override val profile = UserProfile(avatarUrl, bannerUrl, accentColor)

    override val usesDefaultAvatar = avatarHash == null

    /**
     * Whether this user has nitro or not
     */
    val hasNitro = premiumType != User.PremiumType.NONE;

    override fun toString() = "UserCacheEntry(id=$id, name=$name)"
    override fun hashCode() = id.hashCode()
    override fun equals(other: Any?) = other is User && other.id == id

}

data class UserProfile(val avatarUrl: String, val bannerUrl: String?, val accentColor: Color?)