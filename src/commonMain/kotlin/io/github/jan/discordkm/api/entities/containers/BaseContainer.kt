package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity

sealed interface BaseContainer <K, T> : Iterable<T> {

    val values: Collection<T>

    operator fun get(key: K): T?

    override fun iterator() = values.iterator()

}

interface SnowflakeContainer <T : SnowflakeEntity> : BaseContainer<Snowflake, T> {

    override fun get(key: Snowflake) = values.firstOrNull { it.id == key }

}

interface NameableContainer<K, T : Nameable> : BaseContainer<K, T> {

    operator fun get(key: String, ignoreCase: Boolean = false) = values.filter { if(ignoreCase) it.name.lowercase() == key.lowercase() else it.name == key }

}

interface NameableSnowflakeContainer <T> : SnowflakeContainer<T>, NameableContainer<Snowflake, T> where T : SnowflakeEntity, T : Nameable