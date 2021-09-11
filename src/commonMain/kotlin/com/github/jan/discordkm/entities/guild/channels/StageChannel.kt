package com.github.jan.discordkm.entities.guild.channels

import com.github.jan.discordkm.entities.guild.Guild
import kotlinx.serialization.json.JsonObject

class StageChannel(guild: Guild, data: JsonObject) : VoiceChannel(guild, data) {
}