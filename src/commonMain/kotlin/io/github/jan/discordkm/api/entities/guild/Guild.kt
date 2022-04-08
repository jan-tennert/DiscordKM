/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild

import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.Reference
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.UserCacheEntry
import io.github.jan.discordkm.api.entities.activity.Activity
import io.github.jan.discordkm.api.entities.activity.ClientStatus
import io.github.jan.discordkm.api.entities.activity.PresenceStatus
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.clients.WSDiscordClient
import io.github.jan.discordkm.api.entities.clients.WSDiscordClientImpl
import io.github.jan.discordkm.api.entities.containers.CommandContainer
import io.github.jan.discordkm.api.entities.containers.EmoteContainer
import io.github.jan.discordkm.api.entities.containers.GuildChannelContainer
import io.github.jan.discordkm.api.entities.containers.GuildMemberContainer
import io.github.jan.discordkm.api.entities.containers.GuildRoleContainer
import io.github.jan.discordkm.api.entities.containers.GuildThreadContainer
import io.github.jan.discordkm.api.entities.containers.ScheduledEventContainer
import io.github.jan.discordkm.api.entities.containers.StickerContainer
import io.github.jan.discordkm.api.entities.guild.auditlog.AuditLog
import io.github.jan.discordkm.api.entities.guild.auditlog.AuditLogAction
import io.github.jan.discordkm.api.entities.guild.invites.Invite
import io.github.jan.discordkm.api.entities.guild.invites.InviteBuilder
import io.github.jan.discordkm.api.entities.guild.templates.GuildTemplate
import io.github.jan.discordkm.api.entities.guild.welcome.screen.WelcomeScreen
import io.github.jan.discordkm.api.entities.guild.welcome.screen.WelcomeScreenModifier
import io.github.jan.discordkm.api.entities.interactions.CommandHolder
import io.github.jan.discordkm.api.entities.modifiers.Modifiable
import io.github.jan.discordkm.api.entities.modifiers.guild.GuildModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.caching.CacheEntity
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.FlagEnum
import io.github.jan.discordkm.internal.serialization.FlagSerializer
import io.github.jan.discordkm.internal.serialization.UpdateVoiceStatePayload
import io.github.jan.discordkm.internal.serialization.serializers.GuildSerializer
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlin.reflect.KProperty

/*
 * A guild can contain channels and members.
 */
sealed interface Guild : SnowflakeEntity, Reference<Guild>, BaseEntity, CacheEntity, CommandHolder,
    Modifiable<GuildModifier, Unit> {

    override val cache: GuildCacheEntry?
        get() = (client as WSDiscordClientImpl).cacheManager.guildCache[id]
    val roles: GuildRoleContainer
        get() = GuildRoleContainer(this)
    val members: GuildMemberContainer
        get() = GuildMemberContainer(this)
    val threads: GuildThreadContainer
        get() = GuildThreadContainer(this)
    val channels: GuildChannelContainer
        get() = GuildChannelContainer(this)
    val emotes: EmoteContainer
        get() = EmoteContainer(this)
    val scheduledEvents: ScheduledEventContainer
        get() = ScheduledEventContainer(this)
    val stickers: StickerContainer
        get() = StickerContainer(this)
    override val commands: CommandContainer
        get() = CommandContainer(this, "/applications/${client.selfUser.id}/guilds/$id/commands")

    /*
     * Retrieves the audit log for this guild.
     * @param userId The user ID to filter the audit log by.
     * @param action The action to filter the audit log by.
     * @param before Filter the audit log before this ID.
     * @param limit The maximum amount of entries to retrieve.
     */
    suspend fun retrieveAuditLogs(
        limit: Int = 50,
        userId: Snowflake? = null,
        before: Snowflake? = null,
        action: AuditLogAction? = null
    ) = client.buildRestAction<AuditLog> {
        route = Route.Guild.GET_AUDIT_LOGS(id).get {
            putOptional("user_id", userId)
            putOptional("action_type", action?.value)
            putOptional("before", before)
            putOptional("limit", limit)
        }

        transform { AuditLog(it.toJsonObject(), this@Guild) }
    }

    /*
     * Retrieves all bans for this guild
     */
    suspend fun retrieveBans(limit: Int = 1000, before: Snowflake? = null, after: Snowflake? = null) = client.buildRestAction<List<Ban>> {
        route = Route.Ban.GET_BANS(id).get {
            put("limit", limit)
            putOptional("before", before)
            putOptional("after", after)
        }
        transform { it.toJsonArray().map { ban -> GuildSerializer.deserializeBan(ban.jsonObject, this@Guild) } }
    }

    /*
     * Leaves the voice channel in this guild (if connected)
     *
     * Requires a websocket connection.
     */
    suspend fun leaveVoiceChannel() = if (client is WSDiscordClient) {
        (client as WSDiscordClient).shardConnections[0]?.send(
            UpdateVoiceStatePayload(
                id,
                null,
                selfMute = false,
                selfDeaf = false
            )
        )
    } else {
        throw UnsupportedOperationException("You can't leave a voice channel without having a gateway connection!")
    }

    /*
     * Retrieves a ban by its id
     */
    suspend fun retrieveBan(userId: Snowflake) = client.buildRestAction<Ban> {
        route = Route.Ban.GET_BAN(id, userId).get()
        transform { GuildSerializer.deserializeBan(it.toJsonObject(), this@Guild) }
    }

    /*
     * Leaves this guild
     */
    suspend fun leave() = client.buildRestAction<Unit> {
        route = Route.User.LEAVE_GUILD(id).delete()
    }

    /*
     * Deletes this guild.
     *
     * The bot must be the owner of the guild.
     */
    suspend fun delete() = client.buildRestAction<Unit> {
        route = Route.Guild.DELETE_GUILD(id).delete()

    }

    /*
     * Creates a new invite for this guild
     * @param channelId The channel to create the invite for.
     */
    suspend fun createInvite(channelId: Snowflake, builder: InviteBuilder.() -> Unit) = client.buildRestAction<Invite> {
        route = Route.Invite.CREATE_CHANNEL_INVITE(channelId)
            .post(InviteBuilder().apply(builder).data)
        transform { Invite(client, it.toJsonObject()) }
    }

    /**
     * Retrieves all templates for this guild
     */
    suspend fun retrieveTemplates() = client.buildRestAction<List<GuildTemplate>> {
        route = Route.Template.GET_GUILD_TEMPLATES(id).get()
        transform { data -> data.toJsonArray().map { GuildSerializer.deserializeGuildTemplate(it.jsonObject, client) } }
    }

    /*
     * Creates a guild template upon this guild
     */
    suspend fun createTemplate(name: String, description: String?) = client.buildRestAction<GuildTemplate> {
        route = Route.Template.CREATE_GUILD_TEMPLATE(id).post(buildJsonObject {
            put("name", name)
            putOptional("description", description)
        })
        transform { GuildSerializer.deserializeGuildTemplate(it.toJsonObject(), client) }
    }

    /*
     * Modifies this guild
     */
    override suspend fun modify(reason: String?, modifier: GuildModifier.() -> Unit) = client.buildRestAction<Unit> {
        route = Route.Guild.MODIFY_GUILD(id).patch(GuildModifier().apply(modifier).data)
        this.reason = reason
    }

    /*
     * Set's the bots nickname. When [nickname] is null, it will reset the nickname (and will use the bot's username).
     */
    suspend fun modifyOwnNickname(nickname: String?, reason: String? = null) = client.buildRestAction<Unit> {
        route = Route.Guild.MODIFY_CURRENT_MEMBER(id).patch(buildJsonObject {
            put("nick", nickname)
        })
        this.reason = reason
    }

    /*
     * Retrieves the welcome screen for this guild
     */
    suspend fun retrieveWelcomeScreen() = client.buildRestAction<WelcomeScreen> {
        route = Route.Guild.GET_WELCOME_SCREEN(id).get()
        transform { GuildSerializer.deserializeWelcomeScreen(it.toJsonObject(), this@Guild) }
    }

    /*
     * Modifies the welcome screen for this guild
     */
    suspend fun modifyWelcomeScreen(builder: WelcomeScreenModifier.() -> Unit) = client.buildRestAction<WelcomeScreen> {
        route = Route.Guild.MODIFY_WELCOME_SCREEN(id).patch(WelcomeScreenModifier(this@Guild).apply(builder).data)
        transform { GuildSerializer.deserializeWelcomeScreen(it.toJsonObject(), this@Guild) }
    }

    /*
     * An unavailable guild is sent on the [ReadyEvent] when the bot is on this guild but the guild currently has some issues and isn't loaded in the cache
     */
    data class Unavailable(val id: Long)

    /*
     * The [NSFWLevel]
     */
    enum class NSFWLevel : EnumWithValue<Int> {
        DEFAULT,
        EXPLICIT,
        SAFE,
        AGE_RESTRICTED;

        override val value: Int
            get() = ordinal

        companion object : EnumWithValueGetter<NSFWLevel, Int>(values())
    }

    /*
     * A guild with a higher premium tier has more features like higher attachment size
     */
    enum class PremiumTier : EnumWithValue<Int> {
        NONE,
        TIER_1,
        TIER_2,
        TIER_3;

        override val value: Int
            get() = ordinal

        companion object : EnumWithValueGetter<PremiumTier, Int>(values())
    }

    /*
     * The [VerificationLevel] for the guild
     */
    enum class VerificationLevel : EnumWithValue<Int> {
        NONE,

        /*
         * The users must have a verified email address
         */
        LOW,

        /*
         * The users must also be registered on Discord for longer than 5 minutes
         */
        MEDIUM,

        /*
         * The users must also be on this discord server for more than 10 minutes
         */
        HIGH,

        /*
         * The users must have a verified phone on their account
         */
        VERY_HIGH;

        override val value: Int
            get() = ordinal

        companion object : EnumWithValueGetter<VerificationLevel, Int>(values())
    }

    /*
     * The [NotificationLevel] sets the default notification level for all guild channels
     */
    enum class NotificationLevel : EnumWithValue<Int> {
        ALL_MESSAGES,
        ONLY_MENTIONS;

        override val value: Int
            get() = ordinal

        companion object : EnumWithValueGetter<NotificationLevel, Int>(values())

    }

    /*
     * The [ExplicitContentFilter] sets which message should be scanned from discord
     */
    enum class ExplicitContentFilter : EnumWithValue<Int> {
        DISABLED,
        MEMBERS_WITHOUT_ROLES,
        ALL_MEMBERS;

        override val value: Int
            get() = ordinal

        companion object : EnumWithValueGetter<ExplicitContentFilter, Int>(values())
    }

    /*
     * A Guild Feature specifies which features a guild can use. For example if a guild has the feature "BANNER" it means that the guild can use the banner feature
     */
    enum class Feature : EnumWithValue<String> {
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
        CHANNEL_BANNER,
        ROLE_ICONS;

        override val value: String
            get() = name

        companion object : EnumWithValueGetter<Feature, String>(values())

    }

    /*+
    See [Discord Docs](https://discord.com/developers/docs/resources/guild#guild-object-mfa-level) for more information
     */
    enum class MfaLevel : EnumWithValue<Int> {
        NONE,
        ELEVATED;

        override val value: Int
            get() = ordinal

        companion object : EnumWithValueGetter<MfaLevel, Int>(values())

    }

    /*
     * Represents a ban on a guild
     *
     * @property user The user that was banned
     * @property reason The reason for the ban
     */
    class Ban(
        override val guild: Guild,
        val reason: String?,
        val user: UserCacheEntry
    ) : GuildEntity

    /*
     * See [Discord Docs](https://discord.com/developers/docs/resources/guild#guild-object-system-channel-flags) for more information
     */
    enum class SystemChannelFlag(override val offset: Int) : FlagEnum<SystemChannelFlag> {
        UNKNOWN(-1),
        SUPPRESS_JOIN_NOTIFICATIONS(0),
        SUPPRESS_PREMIUM_SUBSCRIPTIONS(1),
        SUPPRESS_GUILD_REMINDER_NOTIFICATIONS(2);

        companion object : FlagSerializer<SystemChannelFlag>(values())
    }

    class GuildPresenceCacheEntry(
        val user: User,
        val clientStatus: ClientStatus,
        val status: PresenceStatus,
        val activities: List<Activity>
    ) : BaseEntity {

        override val client: DiscordClient
            get() = user.client

    }

    companion object {
        operator fun invoke(id: Snowflake, client: DiscordClient): Guild = GuildImpl(id, client)
    }

    override fun getValue(ref: Any?, property: KProperty<*>) = client.guilds[id]!!

    override suspend fun retrieve() = client.guilds.retrieve(id)
}

internal class GuildImpl(override val id: Snowflake, override val client: DiscordClient) : Guild {

    override fun equals(other: Any?) = other is Guild && other.id == id
    override fun toString() = "IndependentGuild(id=$id)"
    override fun hashCode() = id.hashCode()

}