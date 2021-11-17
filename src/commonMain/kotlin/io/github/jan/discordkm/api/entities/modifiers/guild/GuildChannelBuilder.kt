package io.github.jan.discordkm.api.entities.modifiers.guild

import io.github.jan.discordkm.api.entities.channels.guild.GuildChannel
import io.github.jan.discordkm.api.entities.modifiers.BaseModifier

interface GuildChannelBuilder <M : BaseModifier, C : GuildChannel> {

    fun create(modifier: M.() -> Unit) : M

}