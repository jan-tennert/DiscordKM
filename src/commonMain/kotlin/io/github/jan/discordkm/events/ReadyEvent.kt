package io.github.jan.discordkm.events

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.guild.Guild

class ReadyEvent(val unavailableGuilds: List<Guild.Unavailable>, override val client: Client) : Event