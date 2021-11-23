package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.channels.guild.GuildChannel
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannelCacheEntry
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.GuildEntity
import io.github.jan.discordkm.api.entities.modifiers.guild.GuildChannelBuilder
import io.github.jan.discordkm.api.entities.modifiers.guild.GuildChannelModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.jsonObject

open class GuildChannelContainer(override val guild: Guild) : GuildEntity {

    /**
     * Returns all guild channels in this guild
     */
    suspend fun retrieveChannels() = guild.client.buildRestAction<List<GuildChannel>> {
        route = Route.Channel.GET_CHANNELS(guild.id).get()
        transform { it.toJsonArray().map { json -> ChannelSerializer.deserialize(json.jsonObject, guild) } }
    }

    /**
     * Creates a guild channel
     * @param type The type
     **/
    suspend inline fun <reified C: GuildChannel, M: GuildChannelModifier, T : GuildChannelBuilder<M, C>> create(type: T, reason: String? = null, noinline builder: M.() -> Unit) : C = guild.client.buildRestAction<C> {
        route = Route.Channel.CREATE_CHANNEL(guild.id).post(type.create(builder).data)
        transform { ChannelSerializer.deserializeChannel(it.toJsonObject(), guild) as C }
        this.reason = reason
    }

}

class CacheGuildChannelContainer(guild: Guild, override val values: Collection<GuildChannelCacheEntry>) : GuildChannelContainer(guild), NameableSnowflakeContainer<GuildChannelCacheEntry>
class CacheChannelContainer(override val values: Collection<GuildChannelCacheEntry>) : NameableSnowflakeContainer<GuildChannelCacheEntry>