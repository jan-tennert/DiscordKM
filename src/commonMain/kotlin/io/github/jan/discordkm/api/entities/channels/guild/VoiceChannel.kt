/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.channels.guild

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.clients.WSDiscordClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.cacheManager
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEventModifiable
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEventVoiceChannel
import io.github.jan.discordkm.api.entities.modifier.Modifiable
import io.github.jan.discordkm.api.entities.modifier.guild.GuildChannelBuilder
import io.github.jan.discordkm.api.entities.modifier.guild.VoiceChannelModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.UpdateVoiceStatePayload
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject

sealed interface VoiceChannel : GuildChannel, Modifiable<VoiceChannelModifier, VoiceChannelCacheEntry>, InvitableGuildChannel, GuildMessageChannel {

    override val type: ChannelType
        get() = ChannelType.GUILD_VOICE
    override val cache: VoiceChannelCacheEntry?
        get() = guild.cache?.cacheManager?.channelCache?.get(id) as? VoiceChannelCacheEntry

    /*
     * Joins this voice channel over the websocket
     */
    suspend fun join() = if(client is WSDiscordClient) {
        (client as WSDiscordClient).shardConnections[0]?.send(UpdateVoiceStatePayload(guild.id, id, selfMute = false, selfDeaf = false))
        Unit
    } else {
        throw UnsupportedOperationException("You can't join a voice channel without having a gateway connection!")
    }

    override suspend fun modify(reason: String?, modifier: VoiceChannelModifier.() -> Unit) = client.buildRestAction<VoiceChannelCacheEntry> {
        route = Route.Channel.MODIFY_CHANNEL(id).patch(VoiceChannelModifier(ChannelType.GUILD_VOICE).apply(modifier).data)
        this.reason = reason
        transform { ChannelSerializer.deserializeChannel(it.toJsonObject(), guild) }
    }

    enum class VideoQualityMode(override val value: Int) : EnumWithValue<Int> {
        AUTO(1),
        FULL(2);

        companion object : EnumWithValueGetter<VideoQualityMode, Int>(values())
    }

    companion object : GuildChannelBuilder<VoiceChannelModifier, VoiceChannel>, ScheduledEventModifiable<ScheduledEventVoiceChannel> {
        override fun createChannel(modifier: VoiceChannelModifier.() -> Unit) = VoiceChannelModifier(ChannelType.GUILD_VOICE).apply(modifier)

        operator fun invoke(id: Snowflake, guild: Guild): VoiceChannel = VoiceChannelImpl(id, guild)
        operator fun invoke(data: JsonObject, guild: Guild) = ChannelSerializer.deserializeChannel<VoiceChannelCacheEntry>(data, guild)

        override fun createScheduledEvent(modifier: ScheduledEventVoiceChannel.() -> Unit) = ScheduledEventVoiceChannel(false).apply(modifier).build()
    }

}

internal class VoiceChannelImpl(override val id: Snowflake, override val guild: Guild) : VoiceChannel {
    override val type: ChannelType
        get() = if(guild.cache?.cacheManager?.channelCache?.get(id) is StageChannelCacheEntry) {
            ChannelType.GUILD_STAGE_VOICE
        } else {
            ChannelType.GUILD_VOICE
        }

    override fun toString(): String = "VoiceChannel(id=$id, type=$type)"
    override fun equals(other: Any?): Boolean = other is VoiceChannel && other.id == id && other.guild.id == guild.id
    override fun hashCode(): Int = id.hashCode()

}