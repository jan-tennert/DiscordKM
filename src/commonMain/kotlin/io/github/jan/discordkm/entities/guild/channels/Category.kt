package io.github.jan.discordkm.entities.guild.channels

import io.github.jan.discordkm.entities.guild.Guild
import kotlinx.serialization.json.JsonObject

class Category(guild: Guild, data: JsonObject) : GuildChannel(guild, data)