/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.lavalink.stats

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LavalinkStats(
    val players: Int,
    val playingPlayers: Int,
    val uptime: Long,
    val memory: MemoryStats,
    val cpu: CpuStats,
    val frameStats: FrameStats? = null
)

@Serializable
data class MemoryStats(
    val free: Long,
    val used: Long,
    val allocated: Long,
    val reservable: Long
)

@Serializable
data class CpuStats(
    val cores: Int,
    val systemLoad: Double,
    val lavalinkLoad: Double
)

@Serializable
data class FrameStats(
    @SerialName("sent")
    val avgFramesSentPerMinute: Int,
    @SerialName("nulled")
    val avgFramesNulledPerMinute: Int,
    @SerialName("deficit")
    val avgFramesDeficitPerMinute: Int
)