package io.github.jan.discordkm.events

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.guild.Guild

class GuildCreateEvent(val guild: Guild, override val client: Client) : Event