package io.github.jan.discordkm.api.entities.channels.guild

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite
import io.github.jan.discordkm.api.entities.modifiers.Modifiable
import io.github.jan.discordkm.api.entities.modifiers.guild.GuildChannelBuilder
import io.github.jan.discordkm.api.entities.modifiers.guild.VoiceChannelModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.entities.channels.Invitable
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.UpdateVoiceStatePayload
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject

interface VoiceChannel : GuildChannel, Modifiable<VoiceChannelModifier, VoiceChannelCacheEntry>, InvitableGuildChannel {

    override val type: ChannelType
        get() = ChannelType.GUILD_VOICE
    override val cache: VoiceChannelCacheEntry?
        get() = guild.cache?.cacheManager?.channelCache?.get(id) as? VoiceChannelCacheEntry

    /**
     * Joins this voice channel over the websocket
     */
    suspend fun join() = if(client is DiscordWebSocketClient) {
        (client as DiscordWebSocketClient).shardConnections[0].send(UpdateVoiceStatePayload(guild.id, id, selfMute = false, selfDeaf = false))
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

    companion object : GuildChannelBuilder<VoiceChannelModifier, VoiceChannel> {
        override fun create(modifier: VoiceChannelModifier.() -> Unit) = VoiceChannelModifier(ChannelType.GUILD_VOICE).apply(modifier)

        operator fun invoke(id: Snowflake, guild: Guild) = guild.client.channels[id] as? VoiceChannelCacheEntry ?: object : VoiceChannel {
            override val guild = guild
            override val id = id
            override val type: ChannelType
                get() = if(guild.cache?.cacheManager?.channelCache?.get(id) is StageChannelCacheEntry) {
                    ChannelType.GUILD_STAGE_VOICE
                } else {
                    ChannelType.GUILD_VOICE
                }
        }
        operator fun invoke(data: JsonObject, guild: Guild) = ChannelSerializer.deserializeChannel<VoiceChannelCacheEntry>(data, guild)
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