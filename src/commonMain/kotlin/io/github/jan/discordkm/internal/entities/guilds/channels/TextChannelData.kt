package io.github.jan.discordkm.internal.entities.guilds.channels

import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.channels.GuildTextChannel
import io.github.jan.discordkm.api.entities.guild.channels.NewsChannel
import io.github.jan.discordkm.api.entities.guild.channels.TextChannel
import io.github.jan.discordkm.api.entities.guild.channels.Thread
import io.github.jan.discordkm.api.entities.guild.channels.modifier.TextChannelModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.entities.channels.ChannelType
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.RestAction
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractGuildEntity
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class TextChannelData(guild: Guild, data: JsonObject) : GuildTextChannelData(guild, data), TextChannel {

    override suspend fun createPrivateThread(
        name: String,
        autoArchiveDuration: Thread.ThreadDuration,
        invitable: Boolean?
    ) = client.buildRestAction<Thread> {
        route = Route.Thread.START_THREAD(id).post(buildJsonObject {
            put("name", name)
            put("auto_archive_duration", autoArchiveDuration.duration.minutes.toInt())
            put("type", ChannelType.GUILD_PRIVATE_THREAD.id)
            put("invitable", invitable)
        })
        transform { it.toJsonObject().extractGuildEntity(guild) }
        onFinish { (guild as GuildData).threadCache[it.id] = it }
        //check permission
    }

}

/**
 * Modifies this news channel
 * A news channel can also be converted to an [NewsChannel] by setting the type parameter [T] to NewsChannel
 */
suspend inline fun <reified T : GuildTextChannel> TextChannel.modify(modifier: TextChannelModifier.() -> Unit = {}): T = client.buildRestAction {
    val type = when(T::class) {
        TextChannel::class -> null
        NewsChannel::class -> 5
        else -> throw IllegalStateException()
    }
    route = RestAction.patch("/channels/$id", TextChannelModifier(type).apply(modifier).build())
    transform {
        when(type) {
            null -> it.toJsonObject().extractGuildEntity<TextChannel>(guild) as T
            5 -> it.toJsonObject().extractGuildEntity<NewsChannel>(guild) as T
            else -> throw IllegalStateException()
        }
    }
    onFinish { (guild as GuildData).channelCache[id] = it }
}