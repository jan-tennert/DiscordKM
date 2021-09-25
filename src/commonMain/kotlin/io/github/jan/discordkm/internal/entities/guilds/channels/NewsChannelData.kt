package io.github.jan.discordkm.internal.entities.guilds.channels

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.channels.NewsChannel
import io.github.jan.discordkm.api.entities.guild.channels.TextChannel
import io.github.jan.discordkm.api.entities.guild.channels.modifier.TextChannelModifier
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.restaction.RestAction
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractGuildEntity
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class NewsChannelData(guild: Guild, data: JsonObject) : GuildTextChannelData(guild, data), NewsChannel {

    override suspend fun follow(targetId: Snowflake) = client.buildRestAction<Unit> {
        action = RestAction.post("/channels/$id/followers", buildJsonObject {
            put("webhook_channel_id", targetId.long)
        })
        transform {}
        //check permission
    }

}

/**
 * Modifies this news channel
 * A news channel can also be converted to an [TextChannel] by setting the type parameter [T] to TextChannel
 */
suspend inline fun <reified T : GuildTextChannelData> NewsChannel.modify(modifier: TextChannelModifier.() -> Unit = {}): T = client.buildRestAction {
    val type = when(T::class) {
        TextChannel::class -> 0
        NewsChannel::class -> null
        else -> throw IllegalStateException()
    }
    action = RestAction.patch("/channels/$id", TextChannelModifier(type).apply(modifier).build())
    transform {
        when(type) {
            0 -> it.toJsonObject().extractGuildEntity<TextChannel>(guild) as T
            null -> it.toJsonObject().extractGuildEntity<NewsChannel>(guild) as T
            else -> throw IllegalStateException()
        }
    }
    onFinish { (guild as GuildData).channelCache[id] = it }
}