package io.github.jan.discordkm.api.entities

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.caching.CacheEntity
import io.github.jan.discordkm.internal.caching.CacheEntry
import io.github.jan.discordkm.internal.entities.DiscordImage
import io.github.jan.discordkm.internal.entities.channels.PrivateChannel
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.FlagSerializer
import io.github.jan.discordkm.internal.serialization.SerializableEnum
import io.github.jan.discordkm.internal.serialization.serializers.UserSerializer
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import io.github.jan.discordkm.internal.utils.extractClientEntity
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.reflect.KProperty

open class User protected constructor(override val id: Snowflake, override val client: Client) : Mentionable, SnowflakeEntity, Reference<User>, BaseEntity, CacheEntity {

    override val asMention: String
        get() = "<@$id>"
    override val cache: UserCacheEntry?
        get() = client.cacheManager.userCache[id]

    /**
     * Creates a new [PrivateChannel]
     */
    suspend fun createPrivateChannel() = client.buildRestAction<PrivateChannel> {
        route = Route.User.CREATE_DM.post(buildJsonObject {
            put("recipient_id", id.long)
        })

        check {
            if (id == client.selfUser.id) throw UnsupportedOperationException("You can't create a private channel with yourself")
        }

        transform { it.toJsonObject().extractClientEntity(client) }

        onFinish {
           // (this@User as UserData).privateChannel = it
        }
    }

    enum class PremiumType : EnumWithValue<Int> {
        NONE,
        NITRO_CLASSIC,
        NITRO;

        override val value: Int
            get() = ordinal

        companion object : EnumWithValueGetter<PremiumType, Int>(values())
    }

    enum class UserFlag(override val offset: Int) : SerializableEnum<UserFlag> {
        DISCORD_EMPLOYEE(0),
        PARTNERED_SERVER_OWNER(1),
        HYPE_SQUAD_EVENTS(2),
        BUG_HUNTER_LEVEL_1(3),
        BUG_HUNTER_LEVEL_2(14),
        HOUSE_BRAVERY(6),
        HOUSE_BRILLIANCE(7),
        HOUSE_BALANCE(8),
        EARLY_SUPPORTER(9),
        TEAM_USER(10),
        VERIFIED_BOT(16),
        EARLY_VERIFIED_BOT_DEVELOPER(17),
        DISCORD_CERTIFIED_MODERATOR(18),
        BOT_HTTP_INTERACTIONS(19),
        UNKNOWN(-1);

        companion object : FlagSerializer<UserFlag>(values())
    }

    override fun getValue(ref: Any?, property: KProperty<*>) = client.users[id]!!

    override suspend fun retrieve() = client.users.retrieve(id)

    companion object {
        fun from(id: Snowflake, client: Client) = User(id, client)
        fun from(data: JsonObject, client: Client) = UserSerializer.deserialize(data, client)
    }

}

/**
 * The user cache entry contains all information given by the Discord API
 *  @param id The id of the user
 *  @param name The name of the user
 *  @param discriminator The discriminator of the user. Example Test#**__1234__**
 *  @param avatarHash The avatar hash of the user
 *  @param isBot Whether the user is a bot
 *  @param isSystem Whether the user is an official discord system user
 *  @param hasMfaEnabled Whether the user has two-factor authentication enabled
 *  @param flags The flags on the user's account
 *  @param premiumType The type of nitro subscription on a user's account
 *  @param publicFlags The public flags on the user's account
 */
data class UserCacheEntry (
    override val id : Snowflake,
    override val name: String,
    val discriminator: String,
    val avatarHash: String?,
    val isBot: Boolean,
    val isSystem: Boolean,
    val hasMfaEnabled: Boolean,
    val flags: Set<UserFlag>,
    val premiumType: PremiumType,
    val publicFlags: Set<UserFlag>,
    override val client: Client
) : User(id, client), CacheEntry, Nameable {

    /**
     * Whether this user has nitro or not
     */
    val hasNitro = premiumType != PremiumType.NONE;

    /**
     * The avatar url of the user
     */
    val avatarUrl = avatarHash?.let { DiscordImage.userAvatar(id, it) }

}