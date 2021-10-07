/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.EnumSerializer
import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.Reference
import io.github.jan.discordkm.api.entities.SerializableEntity
import io.github.jan.discordkm.api.entities.SerializableEnum
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.activity.Activity
import io.github.jan.discordkm.api.entities.activity.PresenceStatus
import io.github.jan.discordkm.api.entities.guild.channels.Thread
import io.github.jan.discordkm.api.entities.guild.invites.Invite
import io.github.jan.discordkm.api.entities.guild.invites.InviteBuilder
import io.github.jan.discordkm.api.entities.guild.templates.GuildTemplate
import io.github.jan.discordkm.api.entities.lists.CommandList
import io.github.jan.discordkm.api.entities.lists.EmojiList
import io.github.jan.discordkm.api.entities.lists.PresenceList
import io.github.jan.discordkm.api.entities.lists.RetrievableChannelList
import io.github.jan.discordkm.api.entities.lists.RetrievableMemberList
import io.github.jan.discordkm.api.entities.lists.RoleList
import io.github.jan.discordkm.api.entities.lists.StickerList
import io.github.jan.discordkm.api.entities.lists.ThreadList
import io.github.jan.discordkm.api.entities.misc.EnumList
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.utils.extractClientEntity
import io.github.jan.discordkm.internal.utils.getId
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.reflect.KProperty

/**
 * A guild can contain channels and members.
 */
interface Guild : SnowflakeEntity, Reference<Guild>, SerializableEntity, Nameable {

    override val id: Snowflake
        get() = data.getId()

    /**
     * The id owner the guild's owner
     */
    val ownerId: Snowflake

    /**
     * Name of the guild
     */
    override val name: String

    /**
     * Icon Url of the guild
     */
    val iconUrl: String?

    /**
     * Icon hash; returned when in the guild template
     */
    val iconHash: String?

    /**
     * All voice states for this server
     */
    val voiceStates: List<VoiceState>

    /**
     * Splash hash
     */
    val splash: String?

    /**
     * Discovery splash is only available if the guild has the [Feature] feature
     */
    val discoverySplash: String?

    /**
     * Afk timeout
     */
    val afkTimeout: TimeSpan

    /**
     * If widgets are enabled on the server or not
     */
    val widgetsEnabled: Boolean

    /**
     * The [VerificationLevel] required for the guild
     */
    val verificationLevel: VerificationLevel

    /**
     * The default [NotificationLevel] for the guild
     */
    val defaultMessageNotificationLevel: NotificationLevel

    /**
     * The [ExplicitContentFilter] level for the guild
     */
    val explicitContentFilter: ExplicitContentFilter

    /**
     * Returns the [Role]s in the guild
     */
    val roles: RoleList

    /**
     * Returns the default role
     */
    val everyoneRole: Role
        get() = roles["@everyone"].first()

    /**
     * Returns the bot in this guild
     */
    val selfMember: Member
        get() = members[client.selfUser.id]!!

    /**
     * In the command list you can get and create new [ApplicationCommands]
     */
    val commands: CommandList
        get() = (this as GuildData).commands

    /**
     * Returns the [Member]s in the guild
     */
    val members: RetrievableMemberList

    /**
     * Gets the owner of the guild from the cache
     */
    val owner: Member?
        get() = members[ownerId]

    /**
     * The custom guild [Emoji]s
     */
    val emojis: EmojiList

    /**
     * Enabled guild [Feature]s
     */
    val features: List<Feature>

    /**
     * Required [MfaLevel] for the guild
     */
    val mfaLevel: MfaLevel
    /**
     * Application id of the guild creator if the guild is bot created
     */
    val applicationId: Snowflake?

    /**
     * The [SystemChannelFlag]s
     */
    val systemChannelFlags: EnumList<SystemChannelFlag>

    /**
     * If this guild is considered as large
     */
    val isLarge: Boolean

    /**
     * If this guild is unavailable due to an outage
     */
    val isUnavailable: Boolean

    /**
     * Total number of members in this guild
     */
    val memberCount: Int?

    /**
     * In the channel list you can get and create guild channels
     */
    val channels: RetrievableChannelList

    /**
     * In the thread list you can get threads
     */
    val threads: ThreadList

    /**
     * The vanity url code for this guild if it has one
     */
    val vanityUrlCode: String?

    /**
     * The description of a community guild
     */
    val description: String?

    /**
     * The banner of this guild
     */
    val bannerUrl: String?

    /**
     * The [PremiumTier] of the guild (server boost level)
     */
    val premiumTier: PremiumTier

    /**
     * The amount of server boosts
     */
    val premiumSubscriptionCount: Int

    /**
     * The preferredLocale of the community guild
     */
    val preferredLocale: String

    val publicUpdatesChannelId: Snowflake?

    /**
     * The [WelcomeScreen] of the guild if available
     */
    val welcomeScreen: WelcomeScreen?

    /**
     * The [NSFWLevel] of the guild
     */
    val nsfwLevel: NSFWLevel

    /**
     * The id of the widget channel
     */
    val widgetChannelId: Snowflake?

    /**
     * The id of the system channel. In there Discord can send things like boosts and welcome messages
     */
    val systemChannelId: Snowflake?

    /**
     * All [StageInstance]s in this guild
     */
    val stageInstances: List<StageInstance>

    /**
     * All [GuildPresence]s in this guild
     */
    val presences: PresenceList

    /**
     * The [Sticker]s of the guild
     */
    val stickers: StickerList

    /**
     * The id of the rules channel.
     */
    val rulesChannelId: Snowflake?

    /**
     * The id of the afk channel.
     */
    val afkChannelId: Snowflake?

    /**
     * Leaves the guild
     */
    suspend fun leave()

    /**
     * Deletes the guild. The bot must be the owner of this guild
     */
    suspend fun delete()

    /**
     * Creates an invite for this channel
     * @param channelId The channel this invite will refer to
     */
    suspend fun createInvite(channelId: Snowflake, builder: InviteBuilder.() -> Unit): Invite

    /**
     * Retrieves all active threads
     */
    suspend fun retrieveActiveThreads() : List<Thread>

    /**
     * Retrieves all bans from the guild

     * Requires the permission [Permission.BAN_MEMBERS]
     */
    suspend fun retrieveBans() : List<Ban>

    /**
     * Retrieves a ban object from the guild
     *
     * Requires the permission [Permission.BAN_MEMBERS]
     */
    suspend fun retrieveBan(userId: Snowflake): Ban

    /**
     * Leaves the current voice channel, if the bot is in a voice channel
     */
    suspend fun leaveVoiceChannel()

    /**
     * Retrieves all guild templates for this guild
     */
    suspend fun retrieveTemplates(): List<GuildTemplate>

    /**
     * Creates a guild template from this guild
     */
    suspend fun createTemplate(name: String, description: String? = null) : GuildTemplate

    /**
     * An unavailable guild is sent on the [ReadyEvent] when the bot is on this guild but the guild currently has some issues and isn't loaded in the cache
     */
    data class Unavailable(val id: Long)

    /**
     * The [NSFWLevel]
     */
    enum class NSFWLevel {
        DEFAULT,
        EXPLICIT,
        SAFE,
        AGE_RESTRICTED
    }

    /**
     * A guild with a higher premium tier has more features like higher attachment size
     */
    enum class PremiumTier {
        NONE,
        TIER_1,
        TIER_2,
        TIER_3
    }

    /**
     * The [VerificationLevel] for the guild
     */
    enum class VerificationLevel {
        NONE,

        /**
         * The users must have a verified email address
         */
        LOW,

        /**
         * The users must also be registered on Discord for longer than 5 minutes
         */
        MEDIUM,

        /**
         * The users must also be on this discord server for more than 10 minutes
         */
        HIGH,

        /**
         * The users must have a verified phone on their account
         */
        VERY_HIGH;
    }

    /**
     * The [NotificationLevel] sets the default notification level for all guild channels
     */
    enum class NotificationLevel {
        ALL_MESSAGES,
        ONLY_MENTIONS
    }

    /**
     * The [ExplicitContentFilter] sets which message should be scanned from discord
     */
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
        NEW_THREAD_PERMISSIONS,
        ROLE_ICONS
    }

    enum class MfaLevel {
        NONE,
        ELEVATED
    }

    class Ban(val guild: Guild, override val data: JsonObject) : SerializableEntity {

        override val client = guild.client

        /**
         * The reason why a member was banned from their guild
         */
        val reason = data.getOrNull<String>("reason")

        /**
         * The user who was banned
         */
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

    class GuildPresence(override val guild: Guild, override val data: JsonObject) : GuildEntity {

        val member = guild.members[data.getValue("user").jsonObject.getOrThrow<Snowflake>("id")]!!

        val status = PresenceStatus.values().first { it.status == data.getOrNull<String>("status") }

        val activities = data.getValue("activities").jsonArray.map { Json { ignoreUnknownKeys = true }.decodeFromJsonElement<Activity>(it.jsonObject) }

    }

    /**
     * A welcome screen is shown when a new user joins a guild and the guild has the feature [Feature.WELCOME_SCREEN_ENABLED]
     */
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

    override suspend fun retrieve() = client.guilds.retrieve(id)
}