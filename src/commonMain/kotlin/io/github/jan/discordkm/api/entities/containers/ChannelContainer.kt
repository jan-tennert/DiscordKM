package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.channels.guild.GuildChannel
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannelCacheEntry
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.GuildEntity
import io.github.jan.discordkm.api.entities.guild.channels.modifier.GuildChannelBuilder
import io.github.jan.discordkm.api.entities.guild.channels.modifier.GuildChannelModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractChannel
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.jsonObject

open class GuildChannelContainer(override val guild: Guild) : GuildEntity {

    /**
     * Returns all guild channels in this guild
     */
    suspend fun retrieveChannels() = guild.client.buildRestAction<List<GuildChannel>> {
        route = Route.Channel.GET_CHANNELS(guild.id).get()
        transform { it.toJsonArray().map { json -> json.jsonObject.extractChannel(client, guild) as GuildChannel } }
    }

    /**
     * Creates a guild channel
     * @param type The type
     **/
    suspend inline fun <reified C: GuildChannel, M: GuildChannelModifier<C>, T : GuildChannelBuilder<C, M>> create(type: T, noinline builder: M.() -> Unit) : C = guild.client.buildRestAction<C> {
        route = Route.Channel.CREATE_CHANNEL(guild.id).post(type.create(builder))
        transform { it.toJsonObject().extractChannel(client, guild) as C }
    }
    //TODO: Change modifier hirarchy
}

class CacheGuildChannelContainer(guild: Guild, override val values: Collection<GuildChannelCacheEntry>) : GuildChannelContainer(guild), NameableSnowflakeContainer<GuildChannelCacheEntry>
class CacheChannelContainer(override val values: Collection<GuildChannelCacheEntry>) : NameableSnowflakeContainer<GuildChannelCacheEntry>