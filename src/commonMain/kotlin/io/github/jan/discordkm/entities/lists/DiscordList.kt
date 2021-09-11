package io.github.jan.discordkm.entities.lists

import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.SnowflakeEntity

sealed interface DiscordList <T : SnowflakeEntity> : Iterable<T> {

    val internalList: List<T>

    /**
     * Gets this object from the cache
     */
    operator fun get(id: Snowflake) = internalList.firstOrNull { it.id == id }

    /**
     * Searches for the object in the cache and returns a list of matching objects
     */
    operator fun get(name: String) : List<T>

    override operator fun iterator() = internalList.iterator()

}