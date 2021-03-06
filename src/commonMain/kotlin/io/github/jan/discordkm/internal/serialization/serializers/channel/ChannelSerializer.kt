/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.serialization.serializers.channel

import com.soywiz.klock.minutes
import com.soywiz.klock.seconds
import io.github.jan.discordkm.api.entities.channels.Channel
import io.github.jan.discordkm.api.entities.channels.ChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.ChannelFlag
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.channels.guild.Category
import io.github.jan.discordkm.api.entities.channels.guild.CategoryCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.CategoryCacheEntryImpl
import io.github.jan.discordkm.api.entities.channels.guild.ForumChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.ForumChannelCacheEntryImpl
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.GuildTextChannel
import io.github.jan.discordkm.api.entities.channels.guild.NewsChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.NewsChannelCacheEntryImpl
import io.github.jan.discordkm.api.entities.channels.guild.StageChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.StageChannelCacheEntryImpl
import io.github.jan.discordkm.api.entities.channels.guild.TextChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.TextChannelCacheEntryImpl
import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.api.entities.channels.guild.ThreadCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.ThreadCacheEntryImpl
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannel
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannelCacheEntryImpl
import io.github.jan.discordkm.api.entities.clients.WSDiscordClient
import io.github.jan.discordkm.api.entities.clients.WSDiscordClientImpl
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite
import io.github.jan.discordkm.api.entities.message.Message
import io.github.jan.discordkm.internal.serialization.GuildEntitySerializer
import io.github.jan.discordkm.internal.utils.boolean
import io.github.jan.discordkm.internal.utils.get
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.long
import io.github.jan.discordkm.internal.utils.snowflake
import io.github.jan.discordkm.internal.utils.string
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

@PublishedApi internal object ChannelSerializer : GuildEntitySerializer<Channel> {

    override fun deserialize(data: JsonObject, value: Guild): GuildChannelCacheEntry = when(ChannelType[data["type"]!!.int]) {
        ChannelType.GUILD_TEXT -> deserializeChannel<TextChannelCacheEntry>(data, value)
        ChannelType.DM -> TODO()
        ChannelType.GUILD_VOICE -> deserializeChannel<VoiceChannelCacheEntry>(data, value)
        ChannelType.GROUP_DM -> TODO()
        ChannelType.GUILD_CATEGORY -> deserializeChannel<CategoryCacheEntry>(data, value)
        ChannelType.GUILD_NEWS -> deserializeChannel<NewsChannelCacheEntry>(data, value)
        ChannelType.GUILD_STORE -> TODO()
        ChannelType.GUILD_NEWS_THREAD -> deserializeChannel<ThreadCacheEntry>(data, value)
        ChannelType.GUILD_PUBLIC_THREAD -> deserializeChannel<ThreadCacheEntry>(data, value)
        ChannelType.GUILD_PRIVATE_THREAD -> deserializeChannel<ThreadCacheEntry>(data, value)
        ChannelType.GUILD_STAGE_VOICE -> deserializeChannel<StageChannelCacheEntry>(data, value)
        ChannelType.GUILD_FORUM -> deserializeChannel<ForumChannelCacheEntry>(data, value)
        else -> TODO()
    }

    inline fun <reified T : ChannelCacheEntry> deserializeChannel(data: JsonObject, guildX: Guild) : T {
        val id = data["id"]!!.snowflake
        val name = data["name", true]?.string //guild
        val type = ChannelType[data["type"]!!.int]
        val position = data["position", true]?.int //guild
        val permissionOverwrites = data["permission_overwrites"]?.jsonArray?.map { PermissionOverwrite(it.jsonObject) }?.toSet()
        val nsfw = data["nsfw", true]?.boolean //guild
        val guild = data["guild_id", true]?.snowflake?.let { Guild(it, guildX.client) } ?: guildX
        val topic = data["topic", true]?.string //guild
        val lastMessageId = data["last_message_id", true]?.snowflake?.let { Message(it, Channel(id, type, guild.client, guild) as MessageChannel) } //message channel
        if(lastMessageId != null && guildX.client is WSDiscordClient) {
            (guildX.client as WSDiscordClientImpl).lastMessages[id] = lastMessageId
        }
        val bitrate = data["bitrate", true]?.int //voice channel
        val userLimit = data["user_limit", true]?.int //voice channel
        val defaultAutoArchiveDuration = data["default_auto_archive_duration", true]?.int?.minutes?.let { Thread.ThreadDuration.raw(it) } //thread
        val slowModeTime = data["rate_limit_per_user", true]?.int?.seconds //guild
        val parentId = data["parent_id", true]?.snowflake //parent for channel
        val regionId = data["rtc_region", true]?.string //guild
        val videoQualityMode = data["video_quality_mode", true]?.int?.let { VoiceChannel.VideoQualityMode.get(it) } //guild
        val channelFlags = data["flags", true]?.long?.let { ChannelFlag.decode(it) } ?: emptySet()
        return when(T::class) {
            TextChannelCacheEntry::class -> TextChannelCacheEntryImpl(guild,
                position!!,
                permissionOverwrites!!,
                slowModeTime!!,
                nsfw ?: false,
                topic,
                defaultAutoArchiveDuration ?: Thread.ThreadDuration.DAY,
                parentId?.let { Category(it, guild) },
                id,
                name!!
            )
            NewsChannelCacheEntry::class -> NewsChannelCacheEntryImpl(guild,
                position!!,
                permissionOverwrites!!,
                slowModeTime!!,
                nsfw ?: false,
                topic,
                defaultAutoArchiveDuration ?: Thread.ThreadDuration.DAY,
                parentId?.let { Category(it, guild) },
                id,
                name!!
            )
            StageChannelCacheEntry::class -> StageChannelCacheEntryImpl(userLimit!!,
                regionId,
                bitrate!!,
                videoQualityMode ?: VoiceChannel.VideoQualityMode.AUTO,
                guild,
                id,
                name!!,
                position!!,
                permissionOverwrites!!,
                parentId?.let { Category(it, guild) },
            )
            VoiceChannelCacheEntry::class -> VoiceChannelCacheEntryImpl(userLimit!!,
                regionId,
                bitrate!!,
                videoQualityMode ?: VoiceChannel.VideoQualityMode.AUTO,
                guild,
                id,
                name!!,
                position!!,
                permissionOverwrites!!,
                parentId?.let { Category(it, guild) })
            CategoryCacheEntry::class -> CategoryCacheEntryImpl(guild, position!!, permissionOverwrites!!, id, name!!)
            ThreadCacheEntry::class -> ThreadCacheEntryImpl(
                guild,
                permissionOverwrites ?: emptySet(),
                slowModeTime!!,
                GuildTextChannel(parentId!!, guild),
                id,
                name!!,
                type,
                Json.decodeFromJsonElement(data["thread_metadata"]!!),
                ChannelFlag.PINNED in channelFlags
            )
            ForumChannelCacheEntry::class -> ForumChannelCacheEntryImpl(
                id,
                guild,
                parentId?.let { Category(it, guild) },
                name!!,
                permissionOverwrites!!,
                position!!,
                topic!!,
                slowModeTime!!,
            )
            else -> TODO()
        } as T
    }

}
