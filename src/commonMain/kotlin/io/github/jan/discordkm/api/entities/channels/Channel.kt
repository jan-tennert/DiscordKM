/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.channels

import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.Mentionable
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.channels.guild.Category
import io.github.jan.discordkm.api.entities.channels.guild.NewsChannel
import io.github.jan.discordkm.api.entities.channels.guild.StageChannel
import io.github.jan.discordkm.api.entities.channels.guild.TextChannel
import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannel
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.caching.CacheEntity
import io.github.jan.discordkm.internal.caching.CacheEntry
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.FlagEnum
import io.github.jan.discordkm.internal.serialization.FlagSerializer
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import kotlinx.serialization.Serializable

interface Channel : SnowflakeEntity, BaseEntity, Mentionable, CacheEntity {

    override val asMention: String
        get() = "<#$id>"
    override val cache: ChannelCacheEntry?

    val type: ChannelType

    /*
     * Deletes this channel
     * @param reason The reason which will be displayed in the audit logs
     */
    suspend fun delete(reason: String? = null) = client.buildRestAction<Unit> {
        route = Route.Channel.DELETE_CHANNEL(id).delete()
        this.reason = reason
    }

    companion object {
        operator fun invoke(id: Snowflake, type: ChannelType, client: DiscordClient, guild: Guild? = null) = client.channels[id] ?: when (type) {
            ChannelType.GUILD_TEXT -> TextChannel(id, guild!!)
            ChannelType.GUILD_VOICE -> VoiceChannel(id, guild!!)
            ChannelType.GUILD_CATEGORY -> Category(id, guild!!)
            ChannelType.GUILD_NEWS -> NewsChannel(id, guild!!)
            ChannelType.GUILD_NEWS_THREAD -> Thread(id, guild!!, ChannelType.GUILD_NEWS_THREAD)
            ChannelType.GUILD_PUBLIC_THREAD -> Thread(id, guild!!, ChannelType.GUILD_PUBLIC_THREAD)
            ChannelType.GUILD_PRIVATE_THREAD -> Thread(id, guild!!, ChannelType.GUILD_PRIVATE_THREAD)
            ChannelType.GUILD_STAGE_VOICE -> StageChannel(id, guild!!)
            ChannelType.UNKNOWN -> client.channels[id]!!
            else -> throw IllegalArgumentException("Unknown channel type: $type")
        }
    }

}

interface ChannelCacheEntry : Channel, CacheEntry

@Serializable(with = ChannelType.Companion::class)
enum class ChannelType(override val value: Int) : EnumWithValue<Int>{
    UNKNOWN(-1),
    GUILD_TEXT(0),
    DM(1),
    GUILD_VOICE(2),
    GROUP_DM(3),
    GUILD_CATEGORY(4),
    GUILD_NEWS(5),
    GUILD_STORE(6),
    GUILD_NEWS_THREAD(10),
    GUILD_PUBLIC_THREAD(11),
    GUILD_PRIVATE_THREAD(12),
    GUILD_STAGE_VOICE(13),
    GUILD_DIRECTORY(14),
    GUILD_FORUM(15);

    companion object : EnumWithValueGetter<ChannelType, Int>(values())
}

enum class ChannelFlag(override val offset: Int): FlagEnum<ChannelFlag> {
    PINNED(1);

    companion object : FlagSerializer<ChannelFlag>(values())

}