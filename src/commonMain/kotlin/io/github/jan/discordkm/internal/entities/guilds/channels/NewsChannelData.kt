package io.github.jan.discordkm.internal.entities.guilds.channels

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.channels.NewsChannel
import io.github.jan.discordkm.api.entities.guild.channels.TextChannel
import io.github.jan.discordkm.api.entities.guild.channels.modifier.TextChannelModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractGuildEntity
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class NewsChannelData(guild: Guild, data: JsonObject) : GuildTextChannelData(guild, data), NewsChannel {

    override suspend fun follow(targetId: Snowflake) = client.buildRestAction<Unit> {
        route = Route.Channel.FOLLOW_CHANNEL(id).post(buildJsonObject {
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

    route = Route.Channel.MODIFY_CHANNEL(id).patch(TextChannelModifier(type).apply(modifier).build())

    transform {
        when(type) {
            0 -> it.toJsonObject().extractGuildEntity<TextChannel>(guild) as T
            null -> it.toJsonObject().extractGuildEntity<NewsChannel>(guild) as T
            else -> throw IllegalStateException()
        }
    }
    onFinish { (guild as GuildData).channelCache[id] = it }
}