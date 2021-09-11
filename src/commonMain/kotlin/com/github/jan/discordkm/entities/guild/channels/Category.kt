package com.github.jan.discordkm.entities.guild.channels

import com.github.jan.discordkm.entities.guild.Guild
import kotlinx.serialization.json.JsonObject

class Category(guild: Guild, data: JsonObject) : GuildChannel(guild, data)