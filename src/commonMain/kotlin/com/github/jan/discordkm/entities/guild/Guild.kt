package com.github.jan.discordkm.entities.guild

import com.soywiz.klock.seconds
import com.github.jan.discordkm.Cache
import com.github.jan.discordkm.Client
import com.github.jan.discordkm.entities.EnumSerializer
import com.github.jan.discordkm.entities.Reference
import com.github.jan.discordkm.entities.SerializableEntity
import com.github.jan.discordkm.entities.SerializableEnum
import com.github.jan.discordkm.entities.Snowflake
import com.github.jan.discordkm.entities.channels.Channel
import com.github.jan.discordkm.entities.guild.channels.Category
import com.github.jan.discordkm.entities.guild.channels.NewsChannel
import com.github.jan.discordkm.entities.guild.channels.StageChannel
import com.github.jan.discordkm.entities.guild.channels.TextChannel
import com.github.jan.discordkm.entities.guild.channels.VoiceChannel
import com.github.jan.discordkm.entities.misc.ChannelList
import com.github.jan.discordkm.entities.misc.MemberList
import com.github.jan.discordkm.entities.misc.RoleList
import com.github.jan.discordkm.restaction.RestAction
import com.github.jan.discordkm.restaction.buildRestAction
import com.github.jan.discordkm.utils.extract
import com.github.jan.discordkm.utils.extractGuildEntity
import com.github.jan.discordkm.utils.getEnums
import com.github.jan.discordkm.utils.getId
import com.github.jan.discordkm.utils.getOrDefault
import com.github.jan.discordkm.utils.getOrNull
import com.github.jan.discordkm.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

class Guild (override val client: Client, override val data: JsonObject) : Snowflake, Reference<Guild>, SerializableEntity {

    override val id = data.getId()

    @PublishedApi
    internal var roleCache = com.github.jan.discordkm.Cache.fromSnowflakeList(data.getValue("roles").jsonArray.map { it.jsonObject.extractGuildEntity<Role>(this) })
    @PublishedApi
    internal var memberCache = com.github.jan.discordkm.Cache.fromSnowflakeList(data.getValue("members").jsonArray.map { it.jsonObject.extractGuildEntity<Member>(this) })
    @PublishedApi
    internal var channelCache = com.github.jan.discordkm.Cache.fromSnowflakeList(data.getValue("channels").jsonArray.map { json ->
        when (Channel.Type.values().first { it.id == json.jsonObject.getOrThrow<Int>("type") }) {
            Channel.Type.GUILD_TEXT -> json.jsonObject.extractGuildEntity<TextChannel>(this)
            Channel.Type.GUILD_VOICE -> json.jsonObject.extractGuildEntity<VoiceChannel>(this)
            Channel.Type.GUILD_CATEGORY -> json.jsonObject.extractGuildEntity<Category>(this)
            Channel.Type.GUILD_NEWS -> json.jsonObject.extractGuildEntity<NewsChannel>(this)
            Channel.Type.GUILD_STORE -> TODO()
            Channel.Type.GUILD_STAGE_VOICE -> json.jsonObject.extractGuildEntity<StageChannel>(this)
            else -> throw IllegalStateException()
        }
    }
    )

    /**
     * Name of the guild
     */
    val name = data.getOrThrow<String>("name")

    /**
     * Icon Url of the guild
     */
    val iconUrl = data.getOrNull<String>("icon")

    /**
     * Icon hash; returned when in the guild template
     */
    val iconHashUrl = data.getOrNull<String>("icon_hash")

    /**
     * Splash hash
     */
    val splash = data.getOrNull<String>("splash")

    /**
     * Discovery splash is only available if the guild has the [Feature] feature
     */
    val discoverySplash = data.getOrNull<String>("discovery_splash")

    /**
     * The id owner the guild's owner
     */
    val ownerId = data.getOrThrow<Long>("owner_id")

    /**
     * Gets the owner of the guild from the cache
     */
    val owner = members.firstOrNull { it.id == ownerId }
    //val permissions only sent when you check for the bot's guilds
    //region deprecated

    /**
     * Returns the afk channel of the guild if available
     */
    //val afkChannel

    /**
     * Afk timeout
     */
    val afkTimeout = data.getOrThrow<Int>("afk_timeout").seconds

    /**
     * If widgets are enabled on the server or not
     */
    @get:JvmName("widgetsEnabled")
    val widgetsEnabled = data.getOrDefault("widget_enabled", false)

    /**
     * The channel that the widget will generate an invite to
     */
    //val widgetChannel

    /**
     * The [VerificationLevel] required for the guild
     */
    val verificationLevel = VerificationLevel.values().first { it.ordinal == data.getOrThrow<Int>("verification_level") }

    /**
     * The default [NotificationLevel] for the guild
     */
    val defaultMessageNotificationLevel = NotificationLevel.values().first { it.ordinal == data.getOrThrow<Int>("default_message_notifications") }

    /**
     * The [ExplicitContentFilter] level for the guild
     */
    val explicitContentFilter = ExplicitContentFilter.values().first { it.ordinal == data.getOrThrow<Int>("explicit_content_filter") }

    /**
     * Returns the [Role]s in the guild
     */
    val roles
        get() = RoleList(this, roleCache.values.toList())

    /**
     * Returns the [Member]s in the guild
     */
    val members
        get() = MemberList(this, memberCache.values.toList())


    /**
     * The custom guild [Emoji]s
     */
    val emojis = data.getValue("emojis").jsonArray.map { it.jsonObject.extract<Emoji>() }

    /**
     * Enabled guild [Feature]s
     */
    val features = data.getValue("features").jsonArray.map { Feature.valueOf(it.jsonPrimitive.content) }

    /**
     * Required [MfaLevel] for the guild
     */
    val mfaLevel = MfaLevel.values().first { it.ordinal == data.getOrThrow<Int>("mfa_level") }

    /**
     * Application id of the guild creator if the guild is bot created
     */
    val applicationId = data.getOrNull<Long>("application_id")

    /**
     * The system channel where system messages will be sent to
     */
    //val systemChannel

    /**
     * The [SystemChannelFlag]s
     */
    val systemChannelFlags = data.getEnums("system_channel_flags", SystemChannelFlag)

    //val rulesChannel

    //joinedAt?

    /**
     * If this guild is considered as large
     */
    @get:JvmName("isLarge")
    val isLarge = data.getOrDefault("large", false)

    /**
     * If this guild is unavailable due to an outage
     */
    @get:JvmName("isUnavailable")
    val isUnavailable = data.getOrDefault("unavailable", false)

    /**
     * Total number of members in this guild
     */
    var memberCount = data.getOrNull<Int>("member_count")
        internal set
    //voice states

    val channels: ChannelList
        get() = ChannelList(this, channelCache.values)

    //presences

    /**
     * The vanity url code for this guild if it has one
     */
    val vanityUrlCode = data.getOrNull<String>("vanity_url_code")

    /**
     * The description of a community guild
     */
    val description = data.getOrNull<String>("description")

    /**
     * The banner of this guild
     */
    val banner = data.getOrNull<String>("banner")

    /**
     * The [PremiumTier] of the guild (server boost level)
     */
    val premiumTier = PremiumTier.values().first { it.ordinal == data.getOrThrow<Int>("premium_tier") }

    /**
     * The amount of server boosts
     */
    val premiumSubscriptionCount = data.getOrDefault("premium_subscription_count", 0)

    /**
     * The preferredLocale of the community guild
     */
    val preferredLocale = data.getOrThrow<String>("preferred_locale")

    //public updates channels

    //max vid users?

    /**
     * The [WelcomeScreen] of the guild if available
     */
    val welcomeScreen = data["welcome_screen"]?.jsonObject?.extractGuildEntity<WelcomeScreen>(this)

    /**
     * The [NSFWLevel] of the guild
     */
    val nsfwLevel = NSFWLevel.values().first { it.ordinal == data.getOrThrow<Int>("nsfw_level") }

    //Stage instances

    /**
     * The [Sticker]s of the guild
     */
    val stickers = data["stickers"]?.jsonArray?.map { Sticker(it.jsonObject, client) } ?: emptyList()

    /**
     * Leaves the guild
     */
    suspend fun leave() = client.buildRestAction<Unit> {
        action = RestAction.Action.delete("/users/@me/guilds/$id")
        onFinish {
            client.guildCache.remove(id)
        }
    }

    override fun toString() = "Guild[id=$id,name=$name]"

    override fun equals(other: Any?): Boolean {
        if(other !is Guild) return false
        return other.id == id
    }

    data class Unavailable(val id: Long)

    enum class NSFWLevel {
        DEFAULT,
        EXPLICIT,
        SAFE,
        AGE_RESTRICTED
    }

    enum class PremiumTier {
        NONE,
        TIER_1,
        TIER_2,
        TIER_3
    }

    enum class VerificationLevel {
        NONE,
        LOW,
        MEDIUM,
        HIGH,
        VERY_HIGH;
    }

    enum class NotificationLevel {
        ALL_MESSAGES,
        ONLY_MENTIONS
    }

    enum class ExplicitContentFilter {
        DISABLED,
        MEMBERS_WITHOUT_ROLES,
        ALL_MEMBERS
    }

    enum class Feature {
        ANIMATED_ICON,
        BANNER,
        COMMERCE,
        COMMUNITY,
        DISCOVERABLE,
        FEATURABLE,
        INVITE_SPLASH,
        MEMBER_VERIFICATION_GATE_ENABLED,
        NEWS,
        PARTNERED,
        PREVIEW_ENABLED,
        VANITY_URL,
        VERIFIED,
        VIP_REGIONS,
        WELCOME_SCREEN_ENABLED,
        TICKETED_EVENTS_ENABLED,
        MONETIZATION_ENABLED,
        MORE_STICKERS,
        THREE_DAY_THREAD_ARCHIVE,
        SEVEN_DAY_THREAD_ARCHIVE,
        PRIVATE_THREADS,
        THREADS_ENABLED,
        NEW_THREAD_PERMISSIONS
    }

    enum class MfaLevel {
        NONE,
        ELEVATED
    }

    enum class SystemChannelFlag(override val offset: Int) : SerializableEnum<SystemChannelFlag> {
        UNKNOWN(-1),
        SUPPRESS_JOIN_NOTIFICATIONS(0),
        SUPPRESS_PREMIUM_SUBSCRIPTIONS(1),
        SUPPRESS_GUILD_REMINDER_NOTIFICATIONS(2);

        companion object : EnumSerializer<SystemChannelFlag> {
            override val values = values().toList()
        }
    }

    class WelcomeScreen(val guild: Guild, data: JsonObject) {

        /**
         * The description of the welcome screen
         */
        val description = data.getOrThrow<String>("description")

        /**
         * The channels shown in the [WelcomeScreen]
         */
        val channels = data.getValue("welcome_channels").jsonArray.map { Channel(guild, it.jsonObject) }

        inner class Channel(val guild: Guild, data: JsonObject) {
            //channel
            /**
             * The description shown for the channel
             */
            val description = data.getOrThrow<String>("description")

            /**
             * The emoji id if the emoji is custom
             */
            val emojiId = data.getOrNull<Long>("emoji_id")

            /**
             * The emoji name if the emoji is custom
             */
            val emojiName = data.getOrNull<String>("emoji_name")
        }

    }

    override fun getValue(ref: Any?, property: KProperty<*>) = client.guilds[id]!!
}