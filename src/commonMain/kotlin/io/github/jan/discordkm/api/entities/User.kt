package io.github.jan.discordkm.api.entities

import com.github.ajalt.colormath.Color
import io.github.jan.discordkm.api.entities.misc.EnumList
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.entities.DiscordImage
import io.github.jan.discordkm.internal.entities.UserData
import io.github.jan.discordkm.internal.entities.channels.PrivateChannel
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractClientEntity
import io.github.jan.discordkm.internal.utils.getColor
import io.github.jan.discordkm.internal.utils.getEnums
import io.github.jan.discordkm.internal.utils.getId
import io.github.jan.discordkm.internal.utils.getOrDefault
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.toJsonObject
import io.github.jan.discordkm.internal.utils.valueOfIndexOrDefault
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

interface User : Mentionable, SnowflakeEntity, Reference<User>, SerializableEntity, Nameable {

    override val id: Snowflake
        get() = data.getId()
    override val asMention: String
        get() = "<@$id>"

    val privateChannel: PrivateChannel?

    /**
     * The name of the user
     */
    override val name: String
        get() = data.getOrThrow<String>("username")

    /**
     * The discriminator of the user. Example Test#**__1234__**
     */
    val discriminator: String
        get() = data.getOrThrow<String>("discriminator")

    /**
     * The avatar url of the user
     */
    val avatarUrl: String
        get() = data.getOrNull<String>("avatar")?.let { DiscordImage.userAvatar(id, it) } ?: DiscordImage.defaultUserAvatar(discriminator.toInt())

    /**
     * If the user is a bot
     */
    val isBot: Boolean
        @get:JvmName("isBot")
        get() = data.getOrDefault("bot", false)

    /**
     * If the user is an official discord system user
     */
    val isSystem: Boolean
        @get:JvmName("isSystem")
        get() = data.getOrDefault("system", false)

    /**
     * If the user has two-factor authentication enabled
     */
    val hasMfaEnabled: Boolean
        @get:JvmName("hasMfaEnabled")
        get() = data.getOrDefault("mfa_enabled", false)

    /**
     * The banner url of the user if available
     */
    val bannerUrl: String?
        get() = data.getOrNull<String>("banner")?.let { DiscordImage.userBanner(id, it) }

    /**
     * The user's banner color if available
     */
    val accentColor: Color?
        get() = if(data.getOrNull<Int>("accent_color") != null) data.getColor("accent_color") else null

    /**
     * The flags on the user's account
     */
    val flags: EnumList<UserFlag>
        get() = data.getEnums("flags", UserFlag)

    /**
     * The type of nitro subscription on a user's account
     */
    val premiumType: PremiumType
        get() = valueOfIndexOrDefault(data.getOrNull("premium_type"), default = PremiumType.NONE)

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
            (this@User as UserData).privateChannel = it
        }
    }

    suspend fun getOrCreatePrivateChannel() = privateChannel ?: createPrivateChannel()

    enum class PremiumType {
        NONE,
        NITRO_CLASSIC,
        NITRO
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

        companion object : EnumSerializer<UserFlag> {
            override val values = values().toList()
        }

    }

    override fun getValue(ref: Any?, property: KProperty<*>) = client.users[id]!!

    override suspend fun retrieve() = client.users.retrieve(id)

}