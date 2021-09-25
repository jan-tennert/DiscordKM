package io.github.jan.discordkm.internal.entities.channels

import io.github.jan.discordkm.api.entities.SerializableEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.guild.GuildEntity
import io.github.jan.discordkm.api.entities.guild.channels.Category
import io.github.jan.discordkm.api.entities.guild.channels.GuildChannel
import io.github.jan.discordkm.api.entities.guild.channels.NewsChannel
import io.github.jan.discordkm.api.entities.guild.channels.TextChannel
import io.github.jan.discordkm.api.entities.guild.channels.Thread
import io.github.jan.discordkm.internal.entities.guilds.GuildData

import io.github.jan.discordkm.internal.restaction.RestAction
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractChannel
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.toJsonObject

interface IParent : SerializableEntity, GuildEntity, SnowflakeEntity {

    val parentId: Snowflake?
        get() = data.getOrNull<Snowflake>("parent_id")

    /**
     * The parent of the guild channel.
     *
     * Can be a category for all channels except (of course) [Category] and [Thread]
     *
     * The parent for threads can be [NewsChannel] or [TextChannel]
     *
     * Can be null if a channel is out of a category
     */
    open val parent: GuildChannel?
        get() = (guild.channels[parentId ?: Snowflake.empty()] as? Category)


    suspend fun retrieveParent() = client.buildRestAction<GuildChannel> {
        action = RestAction.get("/channels/$parentId")
        transform {
            it.toJsonObject().extractChannel(client, guild) as GuildChannel
        }
        onFinish { (guild as GuildData).channelCache[id] = it }
    }

}