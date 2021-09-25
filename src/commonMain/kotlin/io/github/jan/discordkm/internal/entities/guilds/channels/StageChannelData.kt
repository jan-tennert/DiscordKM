package io.github.jan.discordkm.internal.entities.guilds.channels

import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.channels.StageChannel
import io.github.jan.discordkm.api.entities.guild.channels.modifier.VoiceChannelModifier
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.restaction.RestAction
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractGuildEntity
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject

class StageChannelData(guild: Guild, data: JsonObject) : VoiceChannelData(guild, data), StageChannel {

    override suspend fun modify(modifier: VoiceChannelModifier.() -> Unit): StageChannel = client.buildRestAction {
        action = RestAction.patch("/channels/$id", VoiceChannelModifier().apply(modifier).build())
        transform {
            it.toJsonObject().extractGuildEntity(guild)
        }
        onFinish { (guild as GuildData).channelCache[id] = it }
    }

}