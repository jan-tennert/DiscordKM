package com.github.jan.discordkm.events

import com.github.jan.discordkm.Client
import com.github.jan.discordkm.entities.guild.Guild

class GuildCreateEvent(val guild: Guild, override val client: Client) : Event