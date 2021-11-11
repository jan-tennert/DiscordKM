package io.github.jan.discordkm.api.entities.channels.guild

import io.github.jan.discordkm.api.entities.channels.Channel

sealed interface ParentChannel : GuildChannel {

    val parent: Channel?

}