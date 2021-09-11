package io.github.jan.discordkm.entities.guild.channels

import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.entities.guild.channels.modifier.CategoryModifier
import io.github.jan.discordkm.entities.guild.channels.modifier.VoiceChannelModifier
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractGuildEntity
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.json.JsonObject

class Category(guild: Guild, data: JsonObject) : GuildChannel(guild, data) {

    suspend fun modify(modifier: CategoryModifier.() -> Unit = {}): Category = client.buildRestAction<Category> {
        action = RestAction.Action.patch("/channels/$id", CategoryModifier().apply(modifier).build())
        transform {
            it.toJsonObject().extractGuildEntity(guild)
        }
        onFinish { guild.channelCache[id] = it }
    }

}