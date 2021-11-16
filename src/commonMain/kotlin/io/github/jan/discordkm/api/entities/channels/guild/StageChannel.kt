package io.github.jan.discordkm.api.entities.channels.guild

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.StageInstance
import io.github.jan.discordkm.api.entities.guild.channels.PermissionOverwrite
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface StageChannel : VoiceChannel {

    override val type: ChannelType
        get() = ChannelType.GUILD_STAGE_VOICE
    override val cache: StageChannelCacheEntry?
        get() = guild.cache?.cacheManager?.channelCache?.get(id) as? StageChannelCacheEntry

    /**
     * Creates a new stage instance in this [StageChannel]
     *
     * @param topic The topic of this stage instance
     * @param public Whether this stage instance will be available in Stage Discovery or not
     */
    suspend fun createInstance(topic: String, public: Boolean = false) = client.buildRestAction<StageInstance> {
        route = Route.StageInstance.CREATE_INSTANCE.post(buildJsonObject {
            put("channel_id", id.long)
            put("topic", topic)
            put("privacy_level", if(public) StageInstance.PrivacyLevel.PUBLIC.ordinal else StageInstance.PrivacyLevel.GUILD_ONLY.ordinal)
        })
        transform { StageInstance(it.toJsonObject(), client) }
    }

    /**
     * Retrieves the current stage instance, if this stage channel has one
     */
    suspend fun retrieveInstance() = client.buildRestAction<StageInstance> {
        route = Route.StageInstance.GET_INSTANCE(id).get()
        transform { StageInstance(it.toJsonObject(), client) }
    }

    companion object {
        operator fun invoke(id: Snowflake, guild: Guild) = guild.client.channels[id] ?: object : StageChannel {
            override val guild = guild
            override val id = id
        }
        operator fun invoke(data: JsonObject, guild: Guild) = ChannelSerializer.deserializeChannel<StageChannelCacheEntry>(data, guild)
    }

}

class StageChannelCacheEntry(
    userLimit: Int,
    regionId: String?,
    bitrate: Int,
    videoQualityMode: VoiceChannel.VideoQualityMode,
    override val guild: Guild,
    override val id: Snowflake,
    override val name: String,
    override val position: Int,
    override val permissionOverwrites: Set<PermissionOverwrite>
) : StageChannel, VoiceChannelCacheEntry(userLimit, regionId, bitrate, videoQualityMode, guild, id, name, position, permissionOverwrites)