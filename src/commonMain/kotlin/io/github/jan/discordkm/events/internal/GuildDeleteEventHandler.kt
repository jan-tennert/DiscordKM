package io.github.jan.discordkm.events.internal

import com.soywiz.klogger.Logger
import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.events.GuildDeleteEvent
import io.github.jan.discordkm.utils.getOrThrow
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