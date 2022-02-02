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