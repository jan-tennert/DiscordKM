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
import com.soywiz.klock.seconds
import io.github.jan.discordkm.api.entities.EnumSerializer
import io.github.jan.discordkm.api.entities.Reference
import io.github.jan.discordkm.api.entities.SerializableEntity
import io.github.jan.discordkm.api.entities.SerializableEnum
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.guild.channels.Thread
import io.github.jan.discordkm.api.entities.guild.invites.Invite
import io.github.jan.discordkm.api.entities.guild.invites.InviteBuilder
import io.github.jan.discordkm.api.entities.guild.templates.GuildTemplate
import io.github.jan.discordkm.api.entities.lists.CommandList
import io.github.jan.discordkm.api.entities.lists.RetrievableChannelList
import io.github.jan.discordkm.api.entities.lists.RetrievableMemberList
import io.github.jan.discordkm.api.entities.lists.RoleList
import io.github.jan.discordkm.api.entities.lists.ThreadList
import io.github.jan.discordkm.api.entities.misc.EnumList
import io.github.jan.discordkm.internal.entities.UserData
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.entities.guilds.VoiceStateData
import io.github.jan.discordkm.internal.utils.DiscordImage
import io.github.jan.discordkm.internal.utils.extractClientEntity
import io.github.jan.discordkm.internal.utils.extractGuildEntity
import io.github.jan.discordkm.internal.utils.getEnums
import io.github.jan.discordkm.internal.utils.getId
import io.github.jan.discordkm.internal.utils.getOrDefault
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.toJsonObject
import io.github.jan.discordkm.internal.utils.valueOfIndex
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

interface Guild : SnowflakeEntity, Reference<Guild>, SerializableEntity {

    override val id: Snowflake
        get() = data.getId()

    /**
     * The id owner the guild's owner
     */
    val ownerId: Snowflake
        get() = Snowflake.fromId(data.getOrThrow<Long>("owner_id"))

    /**
     * Name of the guild
     */
    val name: String
        get() = data.getOrThrow<String>("name")

    /**
     * Icon Url of the guild
     */
    val iconUrl: String?
        get() = data.getOrNull<String>("icon")?.let { DiscordImage.guildIcon(id, it) }

    /**
     * Icon hash; returned when in the guild template
     */
    val iconHash: String?
        get() = data.getOrNull<String>("icon_hash")

    /**
     * All voice states for this server
     */
    val voiceStates: List<VoiceState>
        get() = data.getValue("voice_states").jsonArray.map { VoiceStateData(client, it.jsonObject) }

    /**
     * Splash hash
     */
    val splash: String?
        get() = data.getOrNull<String>("splash")?.let { DiscordImage.guildSplash(id, it) }

    /**
     * Discovery splash is only available if the guild has the [Feature] feature
     */
    val discoverySplash: String?
        get() = data.getOrNull<String>("discovery_splash")?.let { DiscordImage.guildDiscoverySplash(id, it) }

    //val permissions only sent when you check for the bot's guilds
    //region deprecated

    /**
     * Returns the afk channel of the guild if available
     */
    //val afkChannel

    /**
     * Afk timeout
     */
    val afkTimeout: TimeSpan
        get() = data.getOrThrow<Int>("afk_timeout").seconds

    /**
     * If widgets are enabled on the server or not
     */
    val widgetsEnabled: Boolean
        @get:JvmName("widgetsEnabled")
        get() = data.getOrDefault("widget_enabled", false)

    /**
     * The channel that the widget will generate an invite to
     */
    //val widgetChannel

    /**
     * The [VerificationLevel] required for the guild
     */
    val verificationLevel: VerificationLevel
        get() = valueOfIndex(data.getOrThrow("verification_level"))

    /**
     * The default [NotificationLevel] for the guild
     */
    val defaultMessageNotificationLevel: NotificationLevel
        get() = valueOfIndex(data.getOrThrow("default_message_notifications"))

    /**
     * The [ExplicitContentFilter] level for the guild
     */
    val explicitContentFilter: ExplicitContentFilter
        get() = valueOfIndex(data.getOrThrow<Int>("explicit_content_filter"))

    /**
     * Returns the [Role]s in the guild
     */
    val roles: RoleList

    /**
     * Returns the default role
     */
    val everyoneRole: Role
        get() = roles["@everyone"].first()

    val selfMember: Member
        get() = members[client.selfUser.id]!!

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
        get() = members.firstOrNull { it.id == ownerId }


    /**
     * The custom guild [Emoji]s
     */
    val emojis: List<Emoji.Emote>
        get() = data.getValue("emojis").jsonArray.map { it.jsonObject.extractClientEntity<Emoji.Emote>(client) }

    /**
     * Enabled guild [Feature]s
     */
    val features: List<Feature>
        get() = data.getValue("features").jsonArray.map { Feature.valueOf(it.jsonPrimitive.content) }

    /**
     * Required [MfaLevel] for the guild
     */
    val mfaLevel: MfaLevel
        get() = valueOfIndex(data.getOrThrow<Int>("mfa_level"))
    /**
     * Application id of the guild creator if the guild is bot created
     */
    val applicationId: Long?
        get() = data.getOrNull<Long>("application_id")

    /**
     * The system channel where system messages will be sent to
     */
    //val systemChannel

    /**
     * The [SystemChannelFlag]s
     */
    val systemChannelFlags: EnumList<SystemChannelFlag>
        get() = data.getEnums("system_channel_flags", SystemChannelFlag)

    //val rulesChannel

    //joinedAt?

    /**
     * If this guild is considered as large
     */
    val isLarge: Boolean
        @get:JvmName("isLarge")
        get() = data.getOrDefault("large", false)

    /**
     * If this guild is unavailable due to an outage
     */
    val isUnavailable: Boolean
        @get:JvmName("isUnavailable")
        get() = data.getOrDefault("unavailable", false)

    /**
     * Total number of members in this guild
     */
    val memberCount: Int?
        get() = data.getOrNull<Int>("member_count")

    val channels: RetrievableChannelList

    val threads: ThreadList

    //presences

    /**
     * The vanity url code for this guild if it has one
     */
    val vanityUrlCode: String?
        get() = data.getOrNull<String>("vanity_url_code")

    /**
     * The description of a community guild
     */
    val description: String?
        get() = data.getOrNull<String>("description")

    /**
     * The banner of this guild
     */
    val bannerUrl: String?
        get() = data.getOrNull<String>("banner")?.let { DiscordImage.guildBanner(id, it) }

    /**
     * The [PremiumTier] of the guild (server boost level)
     */
    val premiumTier: PremiumTier
        get() = valueOfIndex(data.getOrThrow("premium_tier"))

    /**
     * The amount of server boosts
     */
    val premiumSubscriptionCount: Int
        get() = data.getOrDefault("premium_subscription_count", 0)

    /**
     * The preferredLocale of the community guild
     */
    val preferredLocale: String
        get() = data.getOrThrow<String>("preferred_locale")

    //public updates channels

    //max vid users?

    /**
     * The [WelcomeScreen] of the guild if available
     */
    val welcomeScreen: WelcomeScreen?
        get() = data["welcome_screen"]?.jsonObject?.extractGuildEntity<WelcomeScreen>(this)

    /**
     * The [NSFWLevel] of the guild
     */
    val nsfwLevel: NSFWLevel
        get() = valueOfIndex(data.getOrThrow("nsfw_level"))

    /**
     * All [StageInstance]s in this guild
     */
    val stageInstances: List<StageInstance>
        get() = data.getValue("stage_instances").jsonArray.map { StageInstance(this, it.jsonObject) }

    /**
     * The [Sticker]s of the guild
     */
    val stickers: List<Sticker>
        get() = data["stickers"]?.jsonArray?.map { Sticker(it.jsonObject, client) } ?: emptyList()

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
        NEW_THREAD_PERMISSIONS,
        ROLE_ICONS
    }

    enum class MfaLevel {
        NONE,
        ELEVATED
    }

    class Ban(val guild: Guild, override val data: JsonObject) : SerializableEntity {

        override val client = guild.client

        val reason = data.getOrNull<String>("reason")

        val user = data.getOrThrow<String>("user").toJsonObject().extractClientEntity<UserData>(client)

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

    override suspend fun retrieve() = client.guilds.retrieve(id)
}