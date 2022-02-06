package io.github.jan.discordkm.internal.caching

import co.touchlab.stately.collections.IsoMutableMap
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client

class Cache<V>(private val flag: CacheFlag, private val client: Client) : IsoMutableMap<Snowflake, V>() {

    override fun put(key: Snowflake, value: V): V? = if(flag in client.config.enabledCache) super.put(key, value) else value

    override fun putAll(from: Map<out Snowflake, V>) = if(flag in client.config.enabledCache) super.putAll(from.keys.mapIndexed { index, snowflake -> snowflake to from.values.toList()[index] }.toMap()) else Unit

}