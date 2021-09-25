package io.github.jan.discordkm.internal.entities.guilds.channels

import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.channels.Category
import io.github.jan.discordkm.api.entities.guild.channels.modifier.CategoryModifier
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.restaction.RestAction
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractGuildEntity
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject

class CategoryData(guild: Guild, data: JsonObject) : GuildChannelData(guild, data), Category {

    /**
     * Modifies this category
     */
    override suspend fun modify(modifier: CategoryModifier.() -> Unit): Category = client.buildRestAction<Category> {
        action = RestAction.patch("/channels/$id", CategoryModifier().apply(modifier).build())
        transform {
            it.toJsonObject().extractGuildEntity(guild)
        }
        onFinish { (guild as GuildData).channelCache[id] = it }
    }

}