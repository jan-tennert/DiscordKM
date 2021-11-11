package io.github.jan.discordkm.internal.serialization.serializers

import com.soywiz.klock.seconds
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.containers.CacheGuildRoleContainer
import io.github.jan.discordkm.api.entities.containers.GuildRoleContainer
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.GuildCacheEntry
import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.api.entities.lists.RoleList
import io.github.jan.discordkm.internal.serialization.BaseEntitySerializer
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
        val basicGuild = Guild(data["id"]!!.snowflake, client)
        return GuildCacheEntry(
            id = data["id"]!!.snowflake,
            name = data["name"]!!.string,
            iconHash = data["icon"]?.string,
            splashHash = data["splash"]?.string,
            afkChannelId = data["afk_channel_id"]?.snowflake,
            afkTimeout = data["afk_timeout"]!!.long.seconds,
            verificationLevel = Guild.VerificationLevel.from(data["verification_level"]!!.int),
            defaultMessageNotifications = Guild.NotificationLevel.from(data["default_message_notifications"]!!.int),
            explicitContentFilter = Guild.ExplicitContentFilter.from(data["explicit_content_filter"]!!.int),
            // emotes = data["emojis"].toString().toLongArray(),
            features = data["features"]!!.jsonArray.map { Guild.Feature.from(it.string) }.toSet(),
            mfaLevel = Guild.MfaLevel.from(data["mfa_level"]!!.int),
            applicationId = data["application_id"]?.snowflake,
            widgetEnabled = data["widget_enabled"]?.boolean ?: false,
            widgetChannelId = data["widget_channel_id"]?.snowflake,
            systemChannelId = data["system_channel_id"]?.snowflake,
            joinedAt = data["joined_at"]?.isoTimestamp,
            isLarge = data["large"]?.boolean ?: false,
            memberCount = data["member_count"]?.int ?: 0,
            //voiceStates = data["voice_states"],
            // members = data["members"].toString().toLongArray(),
            //channels = data["channels"].toString().toLongArray(),
            // presences = data["presences"].toString().toLongArray(),
            vanityUrlCode = data["vanity_url_code"]?.string,
            description = data["description"]?.string,
            bannerHash = data["banner"]?.string,
            premiumTier = Guild.PremiumTier.from(data["premium_tier"]!!.int),
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
        }
    }

}