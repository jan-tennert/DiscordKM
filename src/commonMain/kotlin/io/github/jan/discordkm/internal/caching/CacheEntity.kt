package io.github.jan.discordkm.internal.caching

interface CacheEntity {

    val cache: CacheEntry?
        get() = if(this is CacheEntry) this else fromCache()

    fun fromCache(): CacheEntry?

}