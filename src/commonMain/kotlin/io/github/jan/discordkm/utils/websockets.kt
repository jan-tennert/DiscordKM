package io.github.jan.discordkm.utils

import io.github.jan.discordkm.utils.DiscordKMInfo.DISCORD_API_VERSION
import io.github.jan.discordkm.websocket.Compression
import io.github.jan.discordkm.websocket.Compression.NONE
import io.github.jan.discordkm.websocket.Encoding
import com.soywiz.korio.util.OS

fun generateWebsocketURL(encoding: Encoding = Encoding.JSON, compression: Compression = NONE) = "ws${if(!OS.isNative) "s" else ""}://gateway.discord.gg/?v=${DISCORD_API_VERSION}&encoding=${encoding.name.lowercase()}${if(compression != NONE) "&compress=" + compression.key else ""}"