package com.github.jan.discordkm.events

import com.github.jan.discordkm.Client
import com.github.jan.discordkm.entities.guild.Guild

class ReadyEvent(val unavailableGuilds: List<Guild.Unavailable>, override val client: Client) : Event