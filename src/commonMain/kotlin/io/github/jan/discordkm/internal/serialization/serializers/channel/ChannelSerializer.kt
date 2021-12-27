package io.github.jan.discordkm.internal.serialization.serializers.channel

import com.soywiz.klock.minutes
import com.soywiz.klock.seconds
import io.github.jan.discordkm.api.entities.channels.Channel
import io.github.jan.discordkm.api.entities.channels.ChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.channels.guild.Category
import io.github.jan.discordkm.api.entities.channels.guild.CategoryCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.GuildTextChannel
import io.github.jan.discordkm.api.entities.channels.guild.NewsChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.StageChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.TextChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.api.entities.channels.guild.ThreadCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannel
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannelCacheEntry
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.internal.serialization.GuildEntitySerializer
import io.github.jan.discordkm.internal.utils.boolean
import io.github.jan.discordkm.internal.utils.get
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.snowflake
import io.github.jan.discordkm.internal.utils.string
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

object ChannelSerializer : GuildEntitySerializer<Channel> {

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
        else -> TODO()
    }

    inline fun <reified T : ChannelCacheEntry>deserializeChannel(data: JsonObject, guildX: Guild) : T {
        val id = data["id"]!!.snowflake
        val name = data["name", true]?.string //guild
        val type = ChannelType[data["type"]!!.int]
        val position = data["position", true]?.int //guild
        val permissionOverwrites = data["permission_overwrites"]?.jsonArray?.map { PermissionOverwrite(it.jsonObject) }?.toSet()
        val nsfw = data["nsfw", true]?.boolean //guild
        val guild = data["guild_id", true]?.snowflake?.let { Guild(it, guildX.client) } ?: guildX
        val topic = data["topic", true]?.string //guild
        val lastMessageId = data["last_message_id", true]?.snowflake?.let { Message(it, Channel(id, type, guild.client, guild) as MessageChannel) } //message channel
        val bitrate = data["bitrate", true]?.int //voice channel
        val userLimit = data["user_limit", true]?.int //voice channel
        val defaultAutoArchiveDuration = data["default_auto_archive_duration", true]?.int?.minutes?.let { Thread.ThreadDuration.raw(it) } //thread
        val slowModeTime = data["rate_limit_per_user", true]?.int?.seconds //guild
        val parentId = data["parent_id", true]?.snowflake //parent for channel
        val regionId = data["rtc_region", true]?.string //guild
        val videoQualityMode = data["video_quality_mode", true]?.int?.let { VoiceChannel.VideoQualityMode.get(it) } //guild
        return when(T::class) {
            TextChannelCacheEntry::class -> TextChannelCacheEntry(guild, position!!, permissionOverwrites!!, slowModeTime!!, nsfw ?: false, topic, defaultAutoArchiveDuration ?: Thread.ThreadDuration.DAY, Category(parentId!!, guild), id, lastMessageId, name!!)
            NewsChannelCacheEntry::class -> NewsChannelCacheEntry(guild, position!!, permissionOverwrites!!, slowModeTime!!, nsfw ?: false, topic, defaultAutoArchiveDuration ?: Thread.ThreadDuration.DAY, Category(parentId!!, guild), id, lastMessageId, name!!)
            StageChannelCacheEntry::class -> StageChannelCacheEntry(userLimit!!, regionId, bitrate!!, videoQualityMode ?: VoiceChannel.VideoQualityMode.AUTO, guild, id, name!!, position!!, permissionOverwrites!!)
            VoiceChannelCacheEntry::class -> VoiceChannelCacheEntry(userLimit!!, regionId, bitrate!!, videoQualityMode ?: VoiceChannel.VideoQualityMode.AUTO, guild, id, name!!, position!!, permissionOverwrites!!)
            CategoryCacheEntry::class -> CategoryCacheEntry(guild, position!!, permissionOverwrites!!, id, name!!)
            ThreadCacheEntry::class -> ThreadCacheEntry(guild, permissionOverwrites ?: emptySet(), slowModeTime!!, GuildTextChannel(parentId!!, guild), id, lastMessageId, name!!, type, Json.decodeFromJsonElement(data["thread_metadata"]!!))
            else -> TODO()
        } as T
    }

}
