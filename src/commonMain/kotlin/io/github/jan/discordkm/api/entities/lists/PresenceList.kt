package io.github.jan.discordkm.api.entities.lists

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Guild.GuildPresence

class PresenceList(override val internalMap: Map<Snowflake, GuildPresence>) : SnowflakeList<GuildPresence>