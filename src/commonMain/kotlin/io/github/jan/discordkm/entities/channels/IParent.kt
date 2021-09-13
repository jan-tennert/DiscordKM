package io.github.jan.discordkm.entities.channels

import io.github.jan.discordkm.entities.SerializableEntity
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.SnowflakeEntity
import io.github.jan.discordkm.entities.guild.GuildEntity
import io.github.jan.discordkm.entities.guild.channels.Category
import io.github.jan.discordkm.entities.guild.channels.GuildChannel
import io.github.jan.discordkm.entities.guild.channels.NewsChannel
import io.github.jan.discordkm.entities.guild.channels.TextChannel
import io.github.jan.discordkm.entities.guild.channels.Thread
import io.github.jan.discordkm.restaction.CallsTheAPI
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractChannel
import io.github.jan.discordkm.utils.getOrNull
import io.github.jan.discordkm.utils.toJsonObject

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

    @CallsTheAPI
    suspend fun retrieveParent() = client.buildRestAction<GuildChannel> {
        action = RestAction.Action.get("/channels/$parentId")
        transform {
            it.toJsonObject().extractChannel(client, guild) as GuildChannel
        }
        onFinish { guild.channelCache[id] = it }
    }

}