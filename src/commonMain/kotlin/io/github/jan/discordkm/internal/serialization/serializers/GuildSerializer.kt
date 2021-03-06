/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.serialization.serializers

import com.soywiz.klock.seconds
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.activity.ClientStatus
import io.github.jan.discordkm.api.entities.activity.PresenceStatus
import io.github.jan.discordkm.api.entities.channels.Channel
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.GuildCacheEntry
import io.github.jan.discordkm.api.entities.guild.GuildCacheEntryImpl
import io.github.jan.discordkm.api.entities.guild.member.Member
import io.github.jan.discordkm.api.entities.guild.role.Role
import io.github.jan.discordkm.api.entities.guild.sticker.Sticker
import io.github.jan.discordkm.api.entities.guild.sticker.StickerCacheEntry
import io.github.jan.discordkm.api.entities.guild.sticker.StickerCacheEntryImpl
import io.github.jan.discordkm.api.entities.guild.sticker.StickerType
import io.github.jan.discordkm.api.entities.guild.templates.GuildTemplate
import io.github.jan.discordkm.api.entities.guild.welcome.screen.WelcomeScreen
import io.github.jan.discordkm.internal.serialization.BaseEntitySerializer
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import io.github.jan.discordkm.internal.utils.boolean
import io.github.jan.discordkm.internal.utils.get
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.isoTimestamp
import io.github.jan.discordkm.internal.utils.locale
import io.github.jan.discordkm.internal.utils.long
import io.github.jan.discordkm.internal.utils.snowflake
import io.github.jan.discordkm.internal.utils.string
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

internal object GuildSerializer : BaseEntitySerializer<GuildCacheEntry> {

    override fun deserialize(data: JsonObject, value: DiscordClient): GuildCacheEntry {
        val basicGuild = Guild(data["id"]!!.snowflake, value)
        return GuildCacheEntryImpl(
            id = data["id"]!!.snowflake,
            name = data["name"]!!.string,
            iconHash = data["icon", true]?.string,
            splashHash = data["splash", true]?.string,
            afkChannelId = data["afk_channel_id", true]?.snowflake,
            afkTimeout = data["afk_timeout"]!!.long.seconds,
            verificationLevel = Guild.VerificationLevel[data["verification_level"]!!.int],
            defaultMessageNotifications = Guild.NotificationLevel[data["default_message_notifications"]!!.int],
            explicitContentFilter = Guild.ExplicitContentFilter[data["explicit_content_filter"]!!.int],
            features = data["features"]!!.jsonArray.map(JsonElement::string).toSet(),
            mfaLevel = Guild.MfaLevel[data["mfa_level"]!!.int],
            applicationId = data["application_id", true]?.snowflake,
            widgetEnabled = data["widget_enabled", true]?.boolean ?: false,
            widgetChannelId = data["widget_channel_id", true]?.snowflake,
            systemChannelId = data["system_channel_id", true]?.snowflake,
            joinedAt = data["joined_at", true]?.isoTimestamp,
            isLarge = data["large", true]?.boolean ?: false,
            memberCount = data["member_count", true]?.int ?: 0,
            vanityUrlCode = data["vanity_url_code", true]?.string,
            description = data["description", true]?.string,
            bannerHash = data["banner", true]?.string,
            premiumTier = Guild.PremiumTier[data["premium_tier"]!!.int],
            premiumSubscriptionCount = data["premium_subscription_count", true]?.int ?: 0,
            preferredLocale = data["preferred_locale"]!!.locale,
            publicUpdatesChannelId = data["public_updates_channel_id", true]?.snowflake,
            isUnavailable = data["unavailable", true]?.boolean ?: false,
            client = value,
            welcomeScreen = data["welcome_screen"]?.let { Json.decodeFromJsonElement(it) },
            discoveryHash = data["discovery_hash", true]?.string,
            rulesChannelId = data["rules_channel_id", true]?.snowflake,
            systemChannelFlags = data["system_channel_flags", true]?.long?.let { Guild.SystemChannelFlag.decode(it) }
                ?: setOf(),
            ownerId = data["owner_id"]!!.snowflake,
            hasPremiumProgressBarEnabled = data["premium_progress_bar_enabled", true]?.boolean ?: false,
            nsfwLevel = Guild.NSFWLevel[data["nsfw_level"]!!.int],
        ).apply {
            data["roles"]?.jsonArray?.map { Role(it.jsonObject, basicGuild) }?.let { data ->
                cacheManager.roleCache.putAll(data.associateBy { it.id })
            }
            data["members"]?.jsonArray?.map { Member(it.jsonObject, basicGuild) }?.let { data ->
                cacheManager.memberCache.putAll(data.associateBy { it.id })
            }
            data["threads"]?.jsonArray?.map { Thread(it.jsonObject, basicGuild) }?.let { data ->
                cacheManager.threadCache.putAll(data.associateBy { it.id })
            }
            data["channels"]?.jsonArray?.map { ChannelSerializer.deserialize(it.jsonObject, this) }?.let { data ->
                cacheManager.channelCache.putAll(data.associateBy { it.id })
            }
            data["voice_states"]?.jsonArray?.map { VoiceStateSerializer.deserialize(it.jsonObject, basicGuild) }?.let { data ->
                cacheManager.voiceStates.putAll(data.associateBy { it.user.id })
            }
            data["presences"]?.jsonArray?.map { deserializeGuildPresence(it.jsonObject, value) }?.let { data ->
                cacheManager.presences.putAll(data.associateBy { it.user.id })
            }
            data["emojis"]?.jsonArray?.map { deserializeGuildEmote(it.jsonObject, value) }?.let { data ->
                cacheManager.emoteCache.putAll(data.associateBy { it.id })
            }
            data["guild_scheduled_events"]?.jsonArray?.map { ScheduledEventSerializer.deserialize(it.jsonObject, client) }?.let { data ->
                cacheManager.guildScheduledEventCache.putAll(data.associateBy { it.id })
            }
        }
    }

    fun deserializeBan(data: JsonObject, guild: Guild) = Guild.Ban(
        user = User(data["user"]!!.jsonObject, guild.client),
        reason = data["reason", true]?.string,
        guild = guild
    )

    fun deserializeSticker(data: JsonObject, guild: Guild): StickerCacheEntry = StickerCacheEntryImpl(
        id = data["id"]!!.snowflake,
        packId = data["pack_id", true]?.snowflake,
        name = data["name"]!!.string,
        description = data["description", true]?.string,
        tags = data["tags", true]?.string?.split(", ")?.toSet() ?: emptySet(),
        type = StickerType[data["type"]!!.int],
        formatType = Sticker.FormatType[data["format_type"]!!.int],
        isAvailable = data["available", true]?.boolean ?: false,
        guild = guild,
        creator = data["creator"]?.jsonObject?.let { User(it, guild.client) },
        sortValue = data["sort_value", true]?.int,
    )

    fun deserializeGuildPresence(data: JsonObject, client: DiscordClient) = Guild.GuildPresenceCacheEntry(
        status = PresenceStatus[data["status"]!!.string],
        activities = data["activities"]!!.jsonArray.map { Json { ignoreUnknownKeys = true }.decodeFromJsonElement(it.jsonObject) },
        clientStatus = Json.decodeFromJsonElement<ClientStatus>(data["client_status"]!!.jsonObject),
        user = User(data["user"]!!.jsonObject, client)
    )

    fun deserializeGuildEmote(data: JsonObject, client: DiscordClient) = Emoji.Emote(
        id = data["id"]!!.snowflake,
        name = data["name"]!!.string,
        roles = data["roles"]?.jsonArray?.map { it.snowflake } ?: emptyList(),
        creator = data["user"]?.jsonObject?.let { User(it, client) },
        requiresColons = data["require_colons", true]?.boolean ?: false,
        isManagedByAnIntegration = data["managed", true]?.boolean ?: false,
        isAnimated = data["animated", true]?.boolean ?: false,
        isAvailable = data["available", true]?.boolean ?: false,
        client = client
    )

    fun deserializeGuildTemplate(data: JsonObject, client: DiscordClient) = GuildTemplate(
        name = data["name"]!!.string,
        description = data["description", true]?.string,
        creator = User(data["user"]!!.jsonObject, client),
        client = client,
        code = data["code"]!!.string,
        usageCount = data["usage_count"]!!.int,
        createdAt = data["created_at"]!!.isoTimestamp,
        updatedAt = data["updated_at"]!!.isoTimestamp,
        sourceGuild = Guild(data["source_guild_id", true]!!.snowflake, client),
        isDirty = data["is_dirty", true]?.boolean ?: false,
    )

    fun deserializeWelcomeScreen(data: JsonObject, guild: Guild) = WelcomeScreen(
        description = data["description", true]?.string,
        channels = data["channels", true]?.jsonArray?.map {
            WelcomeScreen.WelcomeScreenChannel(
                channel = Channel(it.snowflake, ChannelType.UNKNOWN, guild.client, guild),
                description = it.jsonObject["description"]!!.string,
                emoji = it.let {
                    if(it.jsonObject["emoji_id", true]?.snowflake != null) {
                        Emoji.fromEmote(it.jsonObject["emoji_name"]!!.string, it.jsonObject["emoji_id", true]!!.snowflake)
                    } else if(it.jsonObject["emoji_name", true]?.string != null) {
                        Emoji.fromUnicode(it.jsonObject["emoji_name", true]!!.string)
                    } else {
                        null
                    }
                }
            )
        } ?: emptyList(),
    )

}