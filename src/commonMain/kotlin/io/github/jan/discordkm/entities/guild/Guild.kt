/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.guild

import com.soywiz.klock.seconds
import io.github.jan.discordkm.Cache
import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.EnumSerializer
import io.github.jan.discordkm.entities.Reference
import io.github.jan.discordkm.entities.SerializableEntity
import io.github.jan.discordkm.entities.SerializableEnum
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.SnowflakeEntity
import io.github.jan.discordkm.entities.User
import io.github.jan.discordkm.entities.channels.ChannelType
import io.github.jan.discordkm.entities.guild.channels.Category
import io.github.jan.discordkm.entities.guild.channels.NewsChannel
import io.github.jan.discordkm.entities.guild.channels.StageChannel
import io.github.jan.discordkm.entities.guild.channels.TextChannel
import io.github.jan.discordkm.entities.guild.channels.Thread
import io.github.jan.discordkm.entities.guild.channels.VoiceChannel
import io.github.jan.discordkm.entities.lists.RetrievableChannelList
import io.github.jan.discordkm.entities.lists.RetrievableMemberList
import io.github.jan.discordkm.entities.lists.RoleList
import io.github.jan.discordkm.entities.lists.ThreadList
import io.github.jan.discordkm.exceptions.PermissionException
import io.github.jan.discordkm.restaction.CallsTheAPI
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.DiscordImage
import io.github.jan.discordkm.utils.extract
import io.github.jan.discordkm.utils.extractClientEntity
import io.github.jan.discordkm.utils.extractGuildEntity
import io.github.jan.discordkm.utils.getEnums
import io.github.jan.discordkm.utils.getId
import io.github.jan.discordkm.utils.getOrDefault
import io.github.jan.discordkm.utils.getOrNull
import io.github.jan.discordkm.utils.getOrThrow
import io.github.jan.discordkm.utils.toJsonArray
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

class Guild (override val client: Client, override val data: JsonObject) : SnowflakeEntity, Reference<Guild>, SerializableEntity {

    override val id = data.getId()

    /**
     * The id owner the guild's owner
     */
    val ownerId = Snowflake.fromId(data.getOrThrow<Long>("owner_id"))

    @PublishedApi
    internal var roleCache = Cache.fromSnowflakeEntityList(data.getValue("roles").jsonArray.map { it.jsonObject.extractGuildEntity<Role>(this) })
    @PublishedApi
    internal var memberCache = Cache.fromSnowflakeEntityList(data.getValue("members").jsonArray.map { it.jsonObject.extractGuildEntity<Member>(this) })
    @PublishedApi
    internal var channelCache = Cache.fromSnowflakeEntityList(data.getValue("channels").jsonArray.map { json ->
        when (ChannelType.values().first { it.id == json.jsonObject.getOrThrow<Int>("type") }) {
            ChannelType.GUILD_TEXT -> json.jsonObject.extractGuildEntity<TextChannel>(this)
            ChannelType.GUILD_VOICE -> json.jsonObject.extractGuildEntity<VoiceChannel>(this)
            ChannelType.GUILD_CATEGORY -> json.jsonObject.extractGuildEntity<Category>(this)
            ChannelType.GUILD_NEWS -> json.jsonObject.extractGuildEntity<NewsChannel>(this)
            ChannelType.GUILD_STORE -> TODO()
            ChannelType.GUILD_STAGE_VOICE -> json.jsonObject.extractGuildEntity<StageChannel>(this)
            else -> throw IllegalStateException()
        }
    })
    @PublishedApi
    internal var threadCache = Cache.fromSnowflakeEntityList(data.getValue("threads").jsonArray.map { Thread(this, it.jsonObject) })

    /**
     * Name of the guild
     */
    val name = data.getOrThrow<String>("name")

    /**
     * Icon Url of the guild
     */
    val iconUrl = data.getOrNull<String>("icon")?.let { DiscordImage.guildIcon(id, it) }

    /**
     * Icon hash; returned when in the guild template
     */
    val iconHash = data.getOrNull<String>("icon_hash")

    /**
     * Splash hash
     */
    val splash = data.getOrNull<String>("splash")?.let { DiscordImage.guildSplash(id, it) }

    /**
     * Discovery splash is only available if the guild has the [Feature] feature
     */
    val discoverySplash = data.getOrNull<String>("discovery_splash")?.let { DiscordImage.guildDiscoverySplash(id, it) }

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
     * Returns the default role
     */
    val everyoneRole: Role
        get() = roles["@everyone"].first()

    val selfMember: Member
        get() = members[client.selfUser.id]!!

    /**
     * Returns the [Member]s in the guild
     */
    val members
        get() = RetrievableMemberList(this, memberCache.values.toList())

    /**
     * Gets the owner of the guild from the cache
     */
    val owner = members.firstOrNull { it.id == ownerId }


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

    val channels: RetrievableChannelList
        get() = RetrievableChannelList(this, channelCache.values)

    val threads: ThreadList
        get() = ThreadList(threadCache.values)

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
    val banner = data.getOrNull<String>("banner")?.let { DiscordImage.guildBanner(id, it) }

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
    @CallsTheAPI
    suspend fun leave() = client.buildRestAction<Unit> {
        action = RestAction.Action.delete("/users/@me/guilds/$id")
        onFinish {
            client.guildCache.remove(id)
        }
    }

    /**
     * Retrieves all active threads
     */
    @CallsTheAPI
    suspend fun retrieveActiveThreads() = client.buildRestAction<List<Thread>> {
        action = RestAction.Action.get("/guilds/${id}/threads/active")
        transform { it.toJsonObject().getValue("threads").jsonArray.map { thread -> Thread(this@Guild, thread.jsonObject, it.toJsonObject().jsonArray.map { Json.decodeFromString("members") }) }}
        onFinish { it.forEach { thread -> threadCache[thread.id] = thread } }
    }

    /**
     * Kicks the member from the guild.
     *
     * Requires the permission [Permission.KICK_MEMBERS]
     */
    @CallsTheAPI
    suspend fun kick(memberId: Snowflake) = client.buildRestAction<Unit> {
        action = RestAction.Action.delete("/guilds/${id}/members/$memberId")
        transform {}
        onFinish { memberCache.remove(id) }
        check { if(Permission.KICK_MEMBERS !in selfMember.permissions) throw PermissionException("You require the permission KICK_MEMBERS to kick members from a guild") }
    }

    /**
     * Retrieves all bans from the guild
     */
    @CallsTheAPI
    suspend fun retrieveBans() = client.buildRestAction<List<Ban>> {
        action = RestAction.Action.get("/guilds/$id/bans")
        transform { it.toJsonArray().map { ban -> Ban(this@Guild, ban.jsonObject) }}
        check { if(Permission.BAN_MEMBERS !in selfMember.permissions) throw PermissionException("You require the permission BAN_MEMBERS to retrieve bans from a guild")}
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

    class Ban(val guild: Guild, override val data: JsonObject) : SerializableEntity {

        override val client = guild.client

        val reason = data.getOrNull<String>("reason")

        val user = data.getOrThrow<String>("user").toJsonObject().extractClientEntity<User>(client)

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