package com.github.jan.discordkm

import com.github.jan.discordkm.entities.Snowflake

class Cache <T> {

    internal var internalMap = hashMapOf<Long, T>()
    val values: List<T>
        get() = internalMap.values.toList()

    operator fun set(id: Long, value: T) {
        internalMap[id] = value
    }

    fun remove(id: Long) {
        internalMap.remove(id)
    }

    companion object {

        fun <S : Snowflake>fromSnowflakeList(list: List<S>) : com.github.jan.discordkm.Cache<S> {
            val cache = com.github.jan.discordkm.Cache<S>()
            cache.internalMap = hashMapOf<Long, S>().apply { putAll(list.associateBy { it.id }) }
            return cache
        }

    }

}