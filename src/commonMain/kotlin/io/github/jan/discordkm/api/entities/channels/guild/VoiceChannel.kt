package io.github.jan.discordkm.api.entities.channels.guild

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.channels.PermissionOverwrite
import io.github.jan.discordkm.internal.serialization.UpdateVoiceStatePayload
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import kotlinx.serialization.json.JsonObject

interface VoiceChannel : GuildChannel {

    override val type: ChannelType
        get() = ChannelType.GUILD_VOICE

    /**
     * Joins this voice channel over the websocket
     */
    suspend fun join() = if(client is DiscordWebSocketClient) {
        (client as DiscordWebSocketClient).shardConnections[0].send(UpdateVoiceStatePayload(guild.id, id, selfMute = false, selfDeaf = false))
    } else {
        throw UnsupportedOperationException("You can't join a voice channel without having a gateway connection!")
    }

    enum class VideoQualityMode(override val value: Int) : EnumWithValue<Int> {
        AUTO(1),
        FULL(2);

        companion object : EnumWithValueGetter<VideoQualityMode, Int>(values())
    }

    companion object {
        fun from(id: Snowflake, guild: Guild) = object : VoiceChannel {
            override val guild = guild
            override val id = id
        }
        fun from(data: JsonObject, guild: Guild) = ChannelSerializer.deserializeChannel<VoiceChannelCacheEntry>(data, guild)
    }

}

open class VoiceChannelCacheEntry(
    val userLimit: Int,
    val regionId: String?,
    val bitrate: Int,
    val videoQualityMode: VoiceChannel.VideoQualityMode,
    override val guild: Guild,
    override val id: Snowflake,
    override val name: String,
    override val position: Int,
    override val permissionOverwrites: Set<PermissionOverwrite>
) : VoiceChannel, GuildChannelCacheEntry, IPositionable