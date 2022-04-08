/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.utils

import io.github.jan.discordkm.DiscordKMInfo.DISCORD_API_VERSION
import io.github.jan.discordkm.internal.websocket.Compression
import io.github.jan.discordkm.internal.websocket.Compression.NONE
import io.github.jan.discordkm.internal.websocket.Encoding

fun generateWebsocketURL(encoding: Encoding = Encoding.JSON, compression: Compression = NONE) = "wss://gateway.discord.gg/?v=${DISCORD_API_VERSION}&encoding=${encoding.name.lowercase()}${if(compression != NONE) "&compress=" + compression.key else ""}"