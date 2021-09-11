package io.github.jan.discordkm.entities.guild.channels

import io.github.jan.discordkm.entities.guild.Guild
import kotlinx.serialization.json.JsonObject

class StageChannel(guild: Guild, data: JsonObject) : VoiceChannel(guild, data) {
}