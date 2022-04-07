package io.github.jan.discordkm.api.entities.channels.guild

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.clients.WSDiscordClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite
import io.github.jan.discordkm.api.entities.guild.cacheManager
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEventModifiable
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEventVoiceChannel
import io.github.jan.discordkm.api.entities.modifiers.Modifiable
import io.github.jan.discordkm.api.entities.modifiers.guild.GuildChannelBuilder
import io.github.jan.discordkm.api.entities.modifiers.guild.VoiceChannelModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.caching.MessageCacheManager
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

    /**
     * Joins this voice channel over the websocket
     */
    suspend fun join() = if(client is WSDiscordClient) {
        (client as WSDiscordClient).shardConnections[0]?.send(UpdateVoiceStatePayload(guild.id, id, selfMute = false, selfDeaf = false))
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
        override fun create(modifier: VoiceChannelModifier.() -> Unit) = VoiceChannelModifier(ChannelType.GUILD_VOICE).apply(modifier)

        operator fun invoke(id: Snowflake, guild: Guild): VoiceChannel = VoiceChannelImpl(id, guild)
        operator fun invoke(data: JsonObject, guild: Guild) = ChannelSerializer.deserializeChannel<VoiceChannelCacheEntry>(data, guild)

        override fun build(modifier: ScheduledEventVoiceChannel.() -> Unit) = ScheduledEventVoiceChannel(false).apply(modifier).build()
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

open class VoiceChannelCacheEntry(
    val userLimit: Int,
    val regionId: String?,
    val bitrate: Int,
    val videoQualityMode: VoiceChannel.VideoQualityMode,
    final override val guild: Guild,
    override val id: Snowflake,
    override val name: String,
    override val position: Int,
    override val permissionOverwrites: Set<PermissionOverwrite>,
    override val parent: Category?
) : VoiceChannel, GuildChannelCacheEntry, IPositionable, GuildMessageChannelCacheEntry {

    override val cacheManager = MessageCacheManager(guild.client)

    override val slowModeTime: TimeSpan
        get() = throw UnsupportedOperationException("A text channel in a voice chanel cannot have a slow mode enabled")

    override fun toString(): String = "VoiceChannelCacheEntry(id=$id, type=$type, name=$name)"
    override fun equals(other: Any?): Boolean = other is VoiceChannel && other.id == id && other.guild.id == guild.id
    override fun hashCode(): Int = id.hashCode()

}