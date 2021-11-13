package io.github.jan.discordkm.internal.serialization.serializers

import com.soywiz.klock.seconds
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.activity.ClientStatus
import io.github.jan.discordkm.api.entities.activity.PresenceStatus
import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.GuildCacheEntry
import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.internal.serialization.BaseEntitySerializer
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import io.github.jan.discordkm.internal.utils.boolean
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.isoTimestamp
import io.github.jan.discordkm.internal.utils.long
import io.github.jan.discordkm.internal.utils.snowflake
import io.github.jan.discordkm.internal.utils.string
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

object GuildSerializer : BaseEntitySerializer<GuildCacheEntry> {

    override fun deserialize(data: JsonObject, client: Client): GuildCacheEntry {
        val basicGuild = Guild.from(data["id"]!!.snowflake, client)
        return GuildCacheEntry(
            id = data["id"]!!.snowflake,
            name = data["name"]!!.string,
            iconHash = data["icon"]?.string,
            splashHash = data["splash"]?.string,
            afkChannelId = data["afk_channel_id"]?.snowflake,
            afkTimeout = data["afk_timeout"]!!.long.seconds,
            verificationLevel = Guild.VerificationLevel.get(data["verification_level"]!!.int),
            defaultMessageNotifications = Guild.NotificationLevel.get(data["default_message_notifications"]!!.int),
            explicitContentFilter = Guild.ExplicitContentFilter.get(data["explicit_content_filter"]!!.int),
            features = data["features"]!!.jsonArray.map { Guild.Feature.get(it.string) }.toSet(),
            mfaLevel = Guild.MfaLevel[data["mfa_level"]!!.int],
            applicationId = data["application_id"]?.snowflake,
            widgetEnabled = data["widget_enabled"]?.boolean ?: false,
            widgetChannelId = data["widget_channel_id"]?.snowflake,
            systemChannelId = data["system_channel_id"]?.snowflake,
            joinedAt = data["joined_at"]?.isoTimestamp,
            isLarge = data["large"]?.boolean ?: false,
            memberCount = data["member_count"]?.int ?: 0,
            vanityUrlCode = data["vanity_url_code"]?.string,
            description = data["description"]?.string,
            bannerHash = data["banner"]?.string,
            premiumTier = Guild.PremiumTier[data["premium_tier"]!!.int],
            premiumSubscriptionCount = data["premium_subscription_count"]?.int ?: 0,
            preferredLocale = data["preferred_locale"]!!.string,
            publicUpdatesChannelId = data["public_updates_channel_id"]?.snowflake,
            isUnavailable = data["unavailable"]?.boolean ?: false,
            client = client,
            welcomeScreen = data["welcome_screen"]?.let { Json.decodeFromJsonElement(it) },
            discoveryHash = data["discovery_hash"]?.string,
            rulesChannelId = data["rules_channel_id"]?.snowflake,
            systemChannelFlags = data["system_channel_flags"]?.long?.let { Guild.SystemChannelFlag.decode(it) }
                ?: setOf(),
            ownerId = data["owner_id"]!!.snowflake,
        ).apply {
            data["roles"]?.jsonArray?.map { Role.from(it.jsonObject, basicGuild) }?.let { data ->
                cacheManager.roleCache.putAll(data.associateBy { it.id })
            }
            data["members"]?.jsonArray?.map { Member.from(it.jsonObject, basicGuild) }?.let { data ->
                cacheManager.memberCache.putAll(data.associateBy { it.id })
            }
            data["threads"]?.jsonArray?.map { Thread.from(it.jsonObject, basicGuild) }?.let { data ->
                cacheManager.threadCache.putAll(data.associateBy { it.id })
            }
            data["channels"]?.jsonArray?.map { ChannelSerializer.deserialize(it.jsonObject, client) }?.let { data ->
                cacheManager.channelCache.putAll(data.associateBy { it.id })
            }
            data["voice_states"]?.jsonArray?.map { VoiceStateSerializer.deserialize(it.jsonObject, basicGuild) }?.let { data ->
                cacheManager.voiceStates.putAll(data.associateBy { it.user.id })
            }
            data["presences"]?.jsonArray?.map { deserializeGuildPresence(it.jsonObject, client) }?.let { data ->
                cacheManager.presences.putAll(data.associateBy { it.user.id })
            }
            data["emojis"]?.jsonArray?.map { deserializeGuildEmote(it.jsonObject, basicGuild) }?.let { data ->
                cacheManager.emoteCache.putAll(data.associateBy { it.id })
            }
        }
    }

    fun deserializeBan(data: JsonObject, guild: Guild) = Guild.Ban(
        user = User.from(data["user"]!!.jsonObject, guild.client),
        reason = data["reason"]?.string,
        guild = guild
    )

    fun deserializeGuildPresence(data: JsonObject, client: Client) = Guild.GuildPresenceCacheEntry(
        status = PresenceStatus[data["status"]!!.string],
        activities = data["activities"]!!.jsonArray.map { Json.decodeFromJsonElement(it.jsonObject) },
        clientStatus = Json.decodeFromJsonElement<ClientStatus>(data["client_status"]!!.jsonObject),
        user = User.from(data["user"]!!.jsonObject, client)
    )

    fun deserializeGuildEmote(data: JsonObject, guild: Guild) = Emoji.Emote(
        id = data["id"]!!.snowflake,
        name = data["name"]!!.string,
        roles = data["roles"]?.jsonArray?.map { Role.from(it.snowflake, guild) } ?: emptyList(),
        creator = User.from(data["user"]!!.jsonObject, guild.client),
        requiresColons = data["require_colons"]?.boolean ?: false,
        isManagedByAnIntegration = data["managed"]?.boolean ?: false,
        isAnimated = data["animated"]?.boolean ?: false,
        isAvailable = data["available"]?.boolean ?: false,
        client = guild.client
    )

}