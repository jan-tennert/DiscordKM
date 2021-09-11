package io.github.jan.discordkm.entities

import io.github.jan.discordkm.Client
import io.github.jan.discordkm.entities.channels.PrivateChannel
import io.github.jan.discordkm.entities.misc.Color
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractClientEntity
import io.github.jan.discordkm.utils.getEnums
import io.github.jan.discordkm.utils.getId
import io.github.jan.discordkm.utils.getOrDefault
import io.github.jan.discordkm.utils.getOrNull
import io.github.jan.discordkm.utils.getOrThrow
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

class User(override val client: Client, override val data: JsonObject) : Mentionable, Snowflake, Reference<User>, SerializableEntity {

    override val id = data.getId()
    override val asMention = "<@$id>"

    /**
     * The name of the user
     */
    val name = data.getOrThrow<String>("username")

    /**
     * The discriminator of the user. Example Test#**__1234__**
     */
    val discriminator = data.getOrThrow<String>("discriminator")

    /**
     * The avatar url of the user
     */
    val avatarUrl = data.getOrNull<String>("avatar")

    /**
     * If the user is a bot
     */
    @get:JvmName("isBot")
    val isBot = data.getOrDefault("bot", false)

    /**
     * If the user is an official discord system user
     */
    @get:JvmName("isSystem")
    val isSystem = data.getOrDefault("system", false)

    /**
     * If the user has two-factor authentication enabled
     */
    @get:JvmName("hasMfaEnabled")
    val hasMfaEnabled = data.getOrDefault("mfa_enabled", false)

    /**
     * The banner url of the user if available
     */
    val bannerUrl = data.getOrNull<String>("banner")

    /**
     * The user's banner color if available
     */
    val accentColor = if(data.getOrNull<Int>("accent_color") != null) Color(data.getOrThrow("accent_color")) else null

    //locale?

    /**
     * The flags on the user's account
     */
    val flags = data.getEnums("flags", Flag)

    /**
     * The type of nitro subscription on a user's account
     */
    val premiumType = if(data.getOrNull<Int>("premium_type") != null) PremiumType.values().first { it.ordinal == data.getOrThrow("premium_type") } else PremiumType.NONE

    /**
     * Creates a new [PrivateChannel]
     */
    suspend fun createPrivateChannel() = client.buildRestAction<PrivateChannel> {
        action = RestAction.Action.post("/users/@me/channels", buildJsonObject {
            put("recipient_id", id)
        })

        check {
            if (id == client.selfUser.id) return@check UnsupportedOperationException("You can't create a private channel with yourself")
            return@check null
        }

        transform { it.toJsonObject().extractClientEntity(client) }
    }

    override fun toString() = "User[id=$id,name=$name]"

    override fun equals(other: Any?): Boolean {
        if(other !is User) return false
        return other.id == id
    }

    enum class PremiumType {
        NONE,
        NITRO_CLASSIC,
        NITRO
    }

    enum class Flag(override val offset: Int) : SerializableEnum<Flag> {
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
        UNKNOWN(-1);

        companion object : EnumSerializer<Flag> {
            override val values = values().toList()
        }

    }

    override fun getValue(ref: Any?, property: KProperty<*>) = client.users[id]!!

}