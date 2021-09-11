package com.github.jan.discordkm.entities.guild.channels

import com.github.jan.discordkm.entities.guild.Guild
import com.github.jan.discordkm.restaction.RestAction
import com.github.jan.discordkm.restaction.buildRestAction
import com.github.jan.discordkm.utils.extractGuildEntity
import com.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class TextChannel(guild: Guild, data: JsonObject) : GuildTextChannel(guild, data) {

    suspend fun modify(modifier: TextChannelModifier.() -> Unit) = client.buildRestAction<TextChannel> {
        action = RestAction.Action.patch("/channels/$id", Json.encodeToString(TextChannelModifier(originalType = 0).apply(modifier)))
        transform { it.toJsonObject().extractGuildEntity(guild) }
        onFinish { guild.channelCache[id] = it }
    }

    fun asNewsChannel() = NewsChannel(guild, data)

}