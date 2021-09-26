package io.github.jan.discordkm.internal.entities.guilds.channels

import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.channels.VoiceChannel
import io.github.jan.discordkm.api.entities.guild.channels.modifier.VoiceChannelModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractGuildEntity
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject

open class VoiceChannelData(guild: Guild, data: JsonObject) : GuildChannelData(guild, data), VoiceChannel {

    override suspend fun modify(modifier: VoiceChannelModifier.() -> Unit): VoiceChannel = client.buildRestAction<VoiceChannel> {
        route = Route.Channel.MODIFY_CHANNEL(id).patch(VoiceChannelModifier().apply(modifier).build())
        transform {
            it.toJsonObject().extractGuildEntity(guild)
        }
        onFinish { (guild as GuildData).channelCache[id] = it }
    }

}