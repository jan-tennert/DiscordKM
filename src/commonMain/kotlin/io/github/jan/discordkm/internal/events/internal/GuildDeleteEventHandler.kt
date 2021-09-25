package io.github.jan.discordkm.internal.events.internal

import com.soywiz.klogger.Logger
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.events.GuildDeleteEvent
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject

class GuildDeleteEventHandler(val client: Client, private val LOGGER: Logger) : InternalEventHandler<GuildDeleteEvent> {

    override fun handle(data: JsonObject): GuildDeleteEvent {
        if(data.contains("unavailable")) {
            LOGGER.warn { "The guild \"${data.getOrThrow<Long>("id")}\" is unavailable due to an outage" }
        }
        val id = data.getOrThrow<Snowflake>("id")
        client.guildCache.remove(id)
        return GuildDeleteEvent(client, id)
    }

}