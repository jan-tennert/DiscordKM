package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild

class PresenceUpdateEvent(val presence: Guild.GuildPresence, val oldPresence: Guild.GuildPresence?) : Event {

    override val client: Client
        get() = presence.client

}