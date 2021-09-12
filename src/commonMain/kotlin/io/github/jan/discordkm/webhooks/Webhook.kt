package io.github.jan.discordkm.webhooks

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.SerializableEntity
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.SnowflakeEntity
import io.github.jan.discordkm.utils.getId
import io.github.jan.discordkm.utils.getOrNull
import io.github.jan.discordkm.utils.getOrThrow
import kotlinx.serialization.json.JsonObject

//placeholder
class Webhook(override val client: Client, override val data: JsonObject) : SerializableEntity, SnowflakeEntity {

    override val id = data.getId()

    val type = WebhookType.values().first { it.ordinal + 1 == data.getOrThrow<Int>("type") }

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