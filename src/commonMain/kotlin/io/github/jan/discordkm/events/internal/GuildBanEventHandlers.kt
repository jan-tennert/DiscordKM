package io.github.jan.discordkm.events.internal

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.User
import io.github.jan.discordkm.events.BanEvent
import io.github.jan.discordkm.events.GuildBanAddEvent
import io.github.jan.discordkm.events.GuildBanRemoveEvent
import io.github.jan.discordkm.utils.extractClientEntity
import io.github.jan.discordkm.utils.getOrThrow
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.json.JsonObject

class BanEventHandler(val client: Client) {

    inline fun <reified C : BanEvent> handle(data: JsonObject): C {
        val guildId = data.getOrThrow<Snowflake>("guild_id")
        val user = data.getOrThrow<String>("user").toJsonObject().extractClientEntity<User>(client)
        return when(C::class) {
            GuildBanAddEvent::class -> GuildBanAddEvent(client, guildId, user) as C
            GuildBanRemoveEvent::class -> GuildBanRemoveEvent(client, guildId, user) as C
            else -> throw IllegalStateException()
        }
    }

}