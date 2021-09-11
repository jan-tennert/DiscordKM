package io.github.jan.discordkm.entities.guild.channels

import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.entities.guild.channels.modifier.VoiceChannelModifier
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractGuildEntity
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.json.JsonObject

class StageChannel(guild: Guild, data: JsonObject) : VoiceChannel(guild, data) {

    override suspend fun modify(modifier: VoiceChannelModifier.() -> Unit): StageChannel = client.buildRestAction<StageChannel> {
        action = RestAction.Action.patch("/channels/$id", VoiceChannelModifier().apply(modifier).build())
        transform {
            it.toJsonObject().extractGuildEntity(guild)
        }
        onFinish { guild.channelCache[id] = it }
    }

}