package io.github.jan.discordkm.internal.caching

import co.touchlab.stately.collections.IsoMutableMap
import io.github.jan.discordkm.api.entities.clients.Client

class Cache<K, V>(private val flag: CacheFlag, private val client: Client) : IsoMutableMap<K, V>() {

    override fun put(key: K, value: V): V? = if(flag in client.config.enabledCache) super.put(key, value) else value

    override fun putAll(from: Map<out K, V>) = if(flag in client.config.enabledCache) super.putAll(from) else Unit

}