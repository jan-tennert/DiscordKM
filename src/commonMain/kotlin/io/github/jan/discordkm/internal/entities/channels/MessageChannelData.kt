package io.github.jan.discordkm.internal.entities.channels

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.internal.EntityCache
import kotlinx.serialization.json.JsonObject

open class MessageChannelData(override val client: Client, override val data: JsonObject) : MessageChannel {

    override val messageCache: EntityCache<Snowflake, Message> = EntityCache.fromSnowflakeEntityList(emptyList())

}