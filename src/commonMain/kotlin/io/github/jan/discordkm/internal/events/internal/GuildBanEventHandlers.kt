package io.github.jan.discordkm.internal.events.internal

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.events.BanEvent
import io.github.jan.discordkm.api.events.GuildBanAddEvent
import io.github.jan.discordkm.api.events.GuildBanRemoveEvent
import io.github.jan.discordkm.internal.entities.UserData
import io.github.jan.discordkm.internal.utils.extractClientEntity
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject

class BanEventHandler(val client: Client) {

    inline fun <reified C : BanEvent> handle(data: JsonObject): C {
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")]!!
        val user = data.getOrThrow<String>("user").toJsonObject().extractClientEntity<UserData>(client)
        return when(C::class) {
            GuildBanAddEvent::class -> GuildBanAddEvent(guild, user) as C
            GuildBanRemoveEvent::class -> GuildBanRemoveEvent(guild, user) as C
            else -> throw IllegalStateException()
        }
    }

}