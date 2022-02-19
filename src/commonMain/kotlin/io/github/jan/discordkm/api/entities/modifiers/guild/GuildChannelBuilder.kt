package io.github.jan.discordkm.api.entities.modifiers.guild

import io.github.jan.discordkm.api.entities.channels.guild.GuildChannel
import io.github.jan.discordkm.api.entities.modifiers.JsonModifier

interface GuildChannelBuilder <M : JsonModifier, C : GuildChannel> {

    fun create(modifier: M.() -> Unit) : M

}