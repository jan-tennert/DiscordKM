/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.entities.guilds

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.Updatable
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Guild.ExplicitContentFilter
import io.github.jan.discordkm.api.entities.guild.Guild.Feature
import io.github.jan.discordkm.api.entities.guild.Guild.MfaLevel
import io.github.jan.discordkm.api.entities.guild.Guild.NSFWLevel
import io.github.jan.discordkm.api.entities.guild.Guild.NotificationLevel
import io.github.jan.discordkm.api.entities.guild.Guild.PremiumTier
import io.github.jan.discordkm.api.entities.guild.Guild.SystemChannelFlag
import io.github.jan.discordkm.api.entities.guild.Guild.VerificationLevel
import io.github.jan.discordkm.api.entities.guild.Guild.WelcomeScreen
import io.github.jan.discordkm.api.entities.guild.GuildModifier
import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.api.entities.guild.StageInstance
import io.github.jan.discordkm.api.entities.guild.Sticker
import io.github.jan.discordkm.api.entities.guild.VoiceState
import io.github.jan.discordkm.api.entities.guild.auditlog.AuditLog
import io.github.jan.discordkm.api.entities.guild.auditlog.AuditLogAction
import io.github.jan.discordkm.api.entities.guild.channels.Category
import io.github.jan.discordkm.api.entities.guild.channels.NewsChannel
import io.github.jan.discordkm.api.entities.guild.channels.StageChannel
import io.github.jan.discordkm.api.entities.guild.channels.TextChannel
import io.github.jan.discordkm.api.entities.guild.channels.Thread
import io.github.jan.discordkm.api.entities.guild.channels.VoiceChannel
import io.github.jan.discordkm.api.entities.guild.invites.Invite
import io.github.jan.discordkm.api.entities.guild.invites.InviteBuilder
import io.github.jan.discordkm.api.entities.guild.templates.GuildTemplate
import io.github.jan.discordkm.api.entities.interactions.CommandHolder
import io.github.jan.discordkm.api.entities.interactions.commands.ApplicationCommand
import io.github.jan.discordkm.api.entities.lists.CommandList
import io.github.jan.discordkm.api.entities.lists.EmojiList
import io.github.jan.discordkm.api.entities.lists.PresenceList
import io.github.jan.discordkm.api.entities.lists.RetrievableChannelList
import io.github.jan.discordkm.api.entities.lists.RetrievableMemberList
import io.github.jan.discordkm.api.entities.lists.RoleList
import io.github.jan.discordkm.api.entities.lists.StickerList
import io.github.jan.discordkm.api.entities.lists.ThreadList
import io.github.jan.discordkm.api.entities.misc.EnumList
import io.github.jan.discordkm.internal.EntityCache
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.entities.DiscordImage
import io.github.jan.discordkm.internal.entities.channels.ChannelType
import io.github.jan.discordkm.internal.entities.guilds.channels.ThreadData
import io.github.jan.discordkm.internal.entities.guilds.templates.GuildTemplateData
import io.github.jan.discordkm.internal.exceptions.PermissionException
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.UpdateVoiceStatePayload
import io.github.jan.discordkm.internal.utils.extractClientEntity
import io.github.jan.discordkm.internal.utils.extractGuildEntity
import io.github.jan.discordkm.internal.utils.getEnums
import io.github.jan.discordkm.internal.utils.getId
import io.github.jan.discordkm.internal.utils.getOrDefault
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toIsoMap
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import io.github.jan.discordkm.internal.utils.valueOfIndex
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class GuildData(override val client: Client, override val data: JsonObject) : Guild, CommandHolder, Updatable {

    @PublishedApi
    internal val roleCache = EntityCache.fromSnowflakeEntityList((data["roles"]?.jsonArray ?: buildJsonArray {  }).map { it.jsonObject.extractGuildEntity<Role>(this) })
    @PublishedApi
    internal val memberCache = EntityCache.fromSnowflakeEntityList((data["members"]?.jsonArray ?: buildJsonArray {  }).map { it.jsonObject.extractGuildEntity<Member>(this) })
    @PublishedApi
    internal val channelCache = EntityCache.fromSnowflakeEntityList((data["channels"]?.jsonArray ?: buildJsonArray {  }).map { json ->
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
    internal val threadCache = EntityCache.fromSnowflakeEntityList((data["threads"]?.jsonArray ?: buildJsonArray {  }).map { ThreadData(this, it.jsonObject) as Thread })

    internal val stageInstanceCache = EntityCache.fromSnowflakeEntityList(data.getValue("stage_instances").jsonArray.map { StageInstance(client, it.jsonObject) })

    internal val presenceCache = EntityCache((data["presences"]?.jsonArray?.map {
        Guild.GuildPresence(
            this,
            it.jsonObject
        )
    } ?: emptyList()).associateBy { it.member.id }.toIsoMap())

    internal val stickerCache = EntityCache((data["stickers"]?.jsonArray?.map { Sticker(it.jsonObject, client) } ?: emptyList()).associateBy { it.id }.toIsoMap())

    internal val emojiCache = EntityCache((data.getValue("emojis").jsonArray.map { it.jsonObject.extractClientEntity<Emoji.Emote>(client) }).associateBy { it.id }.toIsoMap())

    internal val voiceStateCache = EntityCache(data.getValue("voice_states").jsonArray.map { VoiceStateData(client, it.jsonObject) }.associateBy { it.userId }.toIsoMap())

    override val stickers: StickerList
        get() = StickerList(stickerCache.internalMap)

    override val emojis: EmojiList
        get() = EmojiList(emojiCache.internalMap)

    override val stageInstances: List<StageInstance>
        get() = stageInstanceCache.values

    override val roles: RoleList
        get() = RoleList(this, roleCache.values.associateBy { it.id })

    override val voiceStates: List<VoiceState>
        get() = voiceStateCache.values

    override val members
        get() = RetrievableMemberList(this, memberCache.values.associateBy { it.id })

    override val channels: RetrievableChannelList
        get() = RetrievableChannelList(this, channelCache.values.associateBy { it.id })

    override val threads: ThreadList
        get() = ThreadList(threadCache.values.associateBy { it.id })

    override val presences: PresenceList
        get() = PresenceList(presenceCache.values.associateBy { it.member.id })

    override val id: Snowflake = data.getId()

    override var ownerId: Snowflake = Snowflake.fromId(data.getOrThrow<Long>("owner_id"))

    override var name: String = data.getOrThrow<String>("name")

    override var iconUrl: String? = data.getOrNull<String>("icon")?.let { DiscordImage.guildIcon(id, it) }

    override val iconHash: String? = data.getOrNull<String>("icon_hash")

    override var splash: String? = data.getOrNull<String>("splash")?.let { DiscordImage.guildSplash(id, it) }

    override var discoverySplash: String? = data.getOrNull<String>("discovery_splash")?.let { DiscordImage.guildDiscoverySplash(id, it) }

    override var afkTimeout: TimeSpan = data.getOrThrow<Int>("afk_timeout").seconds

    override var widgetsEnabled: Boolean = data.getOrDefault("widget_enabled", false)

    override var verificationLevel: VerificationLevel = valueOfIndex(data.getOrThrow("verification_level"))

    override var defaultMessageNotificationLevel: NotificationLevel = valueOfIndex(data.getOrThrow("default_message_notifications"))

    override var explicitContentFilter: ExplicitContentFilter = valueOfIndex(data.getOrThrow<Int>("explicit_content_filter"))

    override var features: List<Feature> = data.getValue("features").jsonArray.map { Feature.valueOf(it.jsonPrimitive.content) }

    override var mfaLevel: MfaLevel = valueOfIndex(data.getOrThrow<Int>("mfa_level"))

    override val applicationId: Snowflake? = data.getOrNull<Snowflake>("application_id")

    override var systemChannelFlags: EnumList<SystemChannelFlag> = data.getEnums("system_channel_flags", SystemChannelFlag)

    override val isLarge: Boolean = data.getOrDefault("large", false)

    override val isUnavailable: Boolean = data.getOrDefault("unavailable", false)

    override val memberCount: Int? = data.getOrNull<Int>("member_count")

    override var vanityUrlCode: String? = data.getOrNull<String>("vanity_url_code")

    override var description: String? = data.getOrNull<String>("description")

    override var bannerUrl: String? = data.getOrNull<String>("banner")?.let { DiscordImage.guildBanner(id, it) }

    override var premiumTier: PremiumTier = valueOfIndex(data.getOrThrow("premium_tier"))

    override var premiumSubscriptionCount: Int = data.getOrDefault("premium_subscription_count", 0)

    override var preferredLocale: String = data.getOrThrow<String>("preferred_locale")

    override var publicUpdatesChannelId: Snowflake? = data.getOrNull("public_updates_channel_id")

    override val welcomeScreen: WelcomeScreen? = data["welcome_screen"]?.jsonObject?.extractGuildEntity<WelcomeScreen>(this)

    override var nsfwLevel: NSFWLevel = valueOfIndex(data.getOrThrow("nsfw_level"))

    override var widgetChannelId: Snowflake? = data.getOrNull<Snowflake>("widget_channel_id")

    override var systemChannelId: Snowflake? = data.getOrNull<Snowflake>("system_channel_id")

    override var rulesChannelId: Snowflake? = data.getOrNull<Snowflake>("rules_channel_id")

    override var afkChannelId: Snowflake? = data.getOrNull<Snowflake>("afk_channel_id")

    override fun update(data: JsonObject) {
        //update role cache
        roleCache.internalMap.clear()
        data.getValue("roles").jsonArray.map { RoleData(this@GuildData, it.jsonObject) }.forEach { roleCache[it.id] = it }
        //update emojis & stickers
        emojiCache.internalMap.clear()
        data.getValue("emojis").jsonArray.map { Emoji.Emote(it.jsonObject, client) }.forEach { emojiCache[it.id] = it }
        stickerCache.internalMap.clear()
        data.getValue("stickers").jsonArray.map { Sticker(it.jsonObject, client) }.forEach { stickerCache[it.id] = it }
        //update generic data
        widgetChannelId = data.getOrNull("widget_channel_id")
        systemChannelId = data.getOrNull("system_channel_id")
        premiumTier = valueOfIndex(data.getOrThrow("premium_tier"))
        systemChannelFlags = data.getEnums("system_channel_flags", Guild.SystemChannelFlag)
        discoverySplash = data.getOrNull("discovery_splash")
        ownerId = data.getOrThrow("owner_id")
        bannerUrl = data.getOrNull<String>("banner")?.let { DiscordImage.guildBanner(id, it) }
        features = data.getValue("features").jsonArray.map { Guild.Feature.valueOf(it.jsonPrimitive.content) }
        publicUpdatesChannelId = data.getOrNull("public_updates_channel_id")
        nsfwLevel = valueOfIndex(data.getOrThrow("nsfw_level"))
        verificationLevel = valueOfIndex(data.getOrThrow("verification_level"))
        splash = data.getOrNull("splash")
        afkTimeout = data.getOrThrow<Int>("afk_timeout").seconds
        vanityUrlCode = data.getOrNull("vanity_url_code")
        iconUrl = data.getOrNull<String>("icon")?.let { DiscordImage.guildIcon(id, it) }
        preferredLocale = data.getOrThrow<String>("preferred_locale")
        explicitContentFilter = valueOfIndex(data.getOrThrow<Int>("explicit_content_filter"))
        rulesChannelId = data.getOrNull("rules_channel_id")
        defaultMessageNotificationLevel = valueOfIndex(data.getOrThrow("default_message_notifications"))
        name = data.getOrThrow("name")
        widgetsEnabled = data.getOrThrow("widget_enabled")
        description = data.getOrNull("description")
        premiumSubscriptionCount = data.getOrDefault("premium_subscription_count", 0)
        mfaLevel = valueOfIndex(data.getOrThrow<Int>("mfa_level"))
        afkChannelId = data.getOrNull("afk_channel_id")
    }

    override suspend fun retrieveActiveThreads() = client.buildRestAction<List<Thread>> {
        route = Route.Thread.GET_ACTIVE_THREADS(id).get()
        transform { it.toJsonObject().getValue("threads").jsonArray.map { thread -> ThreadData(this@GuildData, thread.jsonObject, it.toJsonObject().jsonArray.map { Json.decodeFromString("members") }) }}
        onFinish { it.forEach { thread -> threadCache[thread.id] = thread } }
    }

    override suspend fun retrieveAuditLogs(
        userId: Snowflake?,
        before: Snowflake?,
        limit: Int,
        type: AuditLogAction?
    ) = client.buildRestAction<AuditLog> {
        route = Route.Guild.GET_AUDIT_LOGS(id).get {
            putOptional("user_id", userId)
            putOptional("action_type", type?.value)
            putOptional("before", before)
            putOptional("limit", limit)
        }

        transform { AuditLog(it.toJsonObject(), this@GuildData) }
    }

    override suspend fun retrieveBans() = client.buildRestAction<List<Guild.Ban>> {
        route = Route.Ban.GET_BANS(id).get()
        transform { it.toJsonArray().map { ban -> Guild.Ban(this@GuildData, ban.jsonObject) }}
        check { if(Permission.BAN_MEMBERS !in selfMember.permissions) throw PermissionException("You require the permission BAN_MEMBERS to retrieve bans from a guild") }
    }

    override suspend fun leaveVoiceChannel() = if(client is DiscordWebSocketClient) {
        client.shardConnections[0].send(UpdateVoiceStatePayload(id, null, selfMember.isMuted, selfMember.isDeafened))
    } else {
        throw UnsupportedOperationException("You can't leave a voice channel without having a gateway connection!")
    }

    override suspend fun retrieveBan(userId: Snowflake) = client.buildRestAction<Guild.Ban> {
        route = Route.Ban.GET_BAN(id, userId).get()
        transform { Guild.Ban(this@GuildData, it.toJsonObject()) }
        check { if(Permission.BAN_MEMBERS !in selfMember.permissions) throw PermissionException("You require the permission BAN_MEMBERS to retrieve bans from a guild") }
    }

    override suspend fun leave() = client.buildRestAction<Unit> {
        route = Route.User.LEAVE_GUILD(id).delete()
        onFinish {
            client.guildCache.remove(id)
        }
    }

    override suspend fun delete() = client.buildRestAction<Unit> {
        route = Route.Guild.DELETE_GUILD(id).delete()
        transform {  }
    }

    override suspend fun createInvite(channelId: Snowflake, builder: InviteBuilder.() -> Unit) = client.buildRestAction<Invite> {
        route = Route.Invite.CREATE_CHANNEL_INVITE(channelId).post(Json.encodeToJsonElement(InviteBuilder().apply(builder).build()))
        transform { Invite(client, it.toJsonObject()) }
    }

    override suspend fun retrieveTemplates() = client.buildRestAction<List<GuildTemplate>> {
        route = Route.Template.GET_GUILD_TEMPLATES(id).get()
        transform { it.toJsonArray().map { GuildTemplateData(client, it.jsonObject) } }
    }

    override suspend fun createTemplate(name: String, description: String?) = client.buildRestAction<GuildTemplate> {
        route = Route.Template.CREATE_GUILD_TEMPLATE(id).post(buildJsonObject {
            put("name", name)
            putOptional("description", description)
        })
        transform { GuildTemplateData(client, it.toJsonObject()) }
    }

    override suspend fun modify(modifier: GuildModifier.() -> Unit) = client.buildRestAction<Unit> {
        route = Route.Guild.MODIFY_GUILD(id).patch(GuildModifier().apply(modifier).build())
        transform {  }
    }

    override fun toString() = "Guild[id=$id,name=$name]"

    override val commandCache = EntityCache<Snowflake, ApplicationCommand>()

    override val commands: CommandList
        get() = CommandList("/applications/${client.selfUser.id}/guilds/${id}/commands", this, commandCache.values.associateBy { it.id })

    override fun equals(other: Any?): Boolean {
        if(other !is Guild) return false
        return other.id == id
    }


}