package io.github.jan.discordkm.entities.guild.channels

import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.entities.guild.channels.modifier.TextChannelModifier
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractGuildEntity
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.json.JsonObject

class NewsChannel(guild: Guild, data: JsonObject) : GuildTextChannel(guild, data) {

    suspend inline fun <reified T : GuildTextChannel> modify(modifier: TextChannelModifier.() -> Unit = {}): T = client.buildRestAction {
        val type = when(T::class) {
            TextChannel::class -> 0
            NewsChannel::class -> null
            else -> throw IllegalStateException()
        }
        action = RestAction.Action.patch("/channels/$id", TextChannelModifier(type).apply(modifier).build())
        transform {
            when(type) {
                0 -> it.toJsonObject().extractGuildEntity<TextChannel>(guild) as T
                null -> it.toJsonObject().extractGuildEntity<NewsChannel>(guild) as T
                else -> throw IllegalStateException()
            }
        }
        onFinish { guild.channelCache[id] = it }
    }


    fun asTextChannel() = TextChannel(guild, data)

}