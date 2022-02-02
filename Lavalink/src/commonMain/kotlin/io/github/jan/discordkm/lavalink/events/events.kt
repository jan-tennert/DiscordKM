package io.github.jan.discordkm.lavalink.events

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.events.Event
import io.github.jan.discordkm.lavalink.AudioPlayer
import io.github.jan.discordkm.lavalink.LavalinkNode
import io.github.jan.discordkm.lavalink.stats.LavalinkStats
import io.github.jan.discordkm.lavalink.tracks.EncodedTrack

sealed interface TrackEvent : Event {
    val track: EncodedTrack
    val player: AudioPlayer
}

class TrackStartEvent(override val client: Client, override val track: EncodedTrack, override val player: AudioPlayer) : TrackEvent
class TrackEndEvent(override val client: Client, override val track: EncodedTrack, override val player: AudioPlayer, val reason: String) : TrackEvent
class TrackExceptionEvent(override val client: Client, override val track: EncodedTrack, override val player: AudioPlayer, val exception: Throwable) : TrackEvent
class TrackStuckEvent(override val client: Client, override val track: EncodedTrack, override val player: AudioPlayer, val threshold: TimeSpan) : TrackEvent
class LavalinkStatsUpdateEvent(override val client: Client, val node: LavalinkNode, val stats: LavalinkStats) : Event