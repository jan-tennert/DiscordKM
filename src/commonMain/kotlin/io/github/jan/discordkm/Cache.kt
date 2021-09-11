package io.github.jan.discordkm

import co.touchlab.stately.collections.IsoMutableMap
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.SnowflakeEntity

class Cache<T>(internal var internalMap: IsoMutableMap<Snowflake, T> = IsoMutableMap()) {

    val values: List<T>
        get() = internalMap.values.toList()

    operator fun set(id: Snowflake, value: T) {
        internalMap[id] = value
    }

    fun remove(id: Snowflake) {
        internalMap.remove(id)
    }

    companion object {

        fun <S : SnowflakeEntity> fromSnowflakeEntityList(list: List<S>) = Cache(IsoMutableMap { mutableMapOf<Snowflake, S>().apply { putAll(list.associateBy { it.id }) } })

    }

}