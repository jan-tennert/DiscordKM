package io.github.jan.discordkm.api.entities.channels.guild

import io.github.jan.discordkm.api.entities.containers.CacheGuildThreadContainer
import io.github.jan.discordkm.api.entities.containers.GuildThreadContainer
import io.github.jan.discordkm.api.entities.guild.cacheManager

sealed interface ThreadParent : GuildChannel {

    val threads: GuildThreadContainer
        get() = guild.cache?.cacheManager?.threadCache?.filter { it.value.parent?.id == id }?.values?.let {
            CacheGuildThreadContainer(
                guild,
                it
            )
        } ?: CacheGuildThreadContainer(guild, emptyList())

}