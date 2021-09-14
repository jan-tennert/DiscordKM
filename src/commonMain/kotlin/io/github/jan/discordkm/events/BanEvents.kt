package io.github.jan.discordkm.events

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.Snowflake

class GuildBanAddEvent(override val client: Client, override val guildId: Snowflake) : GuildEvent