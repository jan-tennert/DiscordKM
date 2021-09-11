package io.github.jan.discordkm.entities.guild.channels

import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractGuildEntity
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class NewsChannel(guild: Guild, data: JsonObject) : GuildTextChannel(guild, data) {

    suspend fun modify(modifier: TextChannelModifier.() -> Unit) = client.buildRestAction<NewsChannel> {
        action = RestAction.Action.patch("/channels/$id", Json.encodeToString(TextChannelModifier(originalType = 5).apply(modifier)))
        transform { it.toJsonObject().extractGuildEntity<NewsChannel>(guild) }
        onFinish { guild.channelCache[id] = it }
    }

    //crosspost

    fun asTextChannel() = TextChannel(guild, data)

}