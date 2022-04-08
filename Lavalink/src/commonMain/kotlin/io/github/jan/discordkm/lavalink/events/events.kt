/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.lavalink.events

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.events.Event
import io.github.jan.discordkm.lavalink.AudioPlayer
import io.github.jan.discordkm.lavalink.LavalinkNode
import io.github.jan.discordkm.lavalink.stats.LavalinkStats
import io.github.jan.discordkm.lavalink.tracks.EncodedTrack

sealed interface TrackEvent : Event {
    val track: EncodedTrack
    val player: AudioPlayer
}

class TrackStartEvent(override val client: DiscordClient, override val track: EncodedTrack, override val player: AudioPlayer) : TrackEvent
class TrackEndEvent(override val client: DiscordClient, override val track: EncodedTrack, override val player: AudioPlayer, val reason: String) : TrackEvent
class TrackExceptionEvent(override val client: DiscordClient, override val track: EncodedTrack, override val player: AudioPlayer, val exception: Throwable) : TrackEvent
class TrackStuckEvent(override val client: DiscordClient, override val track: EncodedTrack, override val player: AudioPlayer, val threshold: TimeSpan) : TrackEvent
class LavalinkStatsUpdateEvent(override val client: DiscordClient, val node: LavalinkNode, val stats: LavalinkStats) : Event