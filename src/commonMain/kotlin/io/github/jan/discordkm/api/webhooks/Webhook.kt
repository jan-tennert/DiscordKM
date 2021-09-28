package io.github.jan.discordkm.api.webhooks

import io.github.jan.discordkm.api.entities.SerializableEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.internal.utils.getId
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.valueOfIndex
import kotlinx.serialization.json.JsonObject

//placeholder
class Webhook(override val client: Client, override val data: JsonObject) : SerializableEntity, SnowflakeEntity {

    override val id = data.getId()

    val type = valueOfIndex<WebhookType>(data.getOrThrow("type"), 1)

    val guildId = data.getOrNull<Snowflake>("guild_id")

    val channelId = data.getOrNull<Snowflake>("channel_id")
    //user
    val name = data.getOrThrow<String>("name")

    //icon
    //rest

    enum class WebhookType {
        INCOMING,
        CHANNEL_FOLLOWER,
        APPLICATION
    }

}