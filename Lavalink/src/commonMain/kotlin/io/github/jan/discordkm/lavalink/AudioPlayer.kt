/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.lavalink

import io.github.jan.discordkm.api.entities.clients.on
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.internal.restaction.RestAction.Companion.put
import io.github.jan.discordkm.lavalink.events.TrackEndEvent
import io.github.jan.discordkm.lavalink.tracks.AudioTrack
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray

sealed interface AudioPlayer {

    val guild: Guild

    /*
     * The volume of the player
     */
    val volume: Int

    /*
     * The queue for the audio player
     */
    val queue: LavalinkQueue

    /*
     * The current track being played
     */
    val playingTrack: AudioTrack?

    /*
     * Plays the given track
     * @param track The track to play
     * @param queue Whether the track should be queued or not
     */
    suspend fun play(track: AudioTrack, queue: Boolean = true)

    /*
     * Stops the audio player
     */
    suspend fun stop()

    /*
     * Pauses the audio player
     */
    suspend fun pause()

    /*
     * Resumes the audio player
     */
    suspend fun resume()

    /*
     * Skips the current track
     */
    suspend fun skip()

    /*
     * Enables an equalizer for the audio player
     */
    suspend fun setEqualizer(bands: List<Pair<Int, Float>>)

    /*
     * Sets the volume of the audio player
     */
    suspend fun setVolume(volume: Int)

}

internal class AudioPlayerImpl(override val guild: Guild, private val node: LavalinkNode): AudioPlayer {

    override var volume: Int = 100
    override val queue = LavalinkQueueImpl()
    override var playingTrack: AudioTrack? = null
    private val mutex = Mutex()

    init {
        node.client.on<TrackEndEvent> {
            if(reason == "REPLACED") return@on
            if(queue.isNotEmpty() && listOf("FINISHED", "REPLACED").contains(reason)) {
                val nextTrack = queue.removeFirst()
                mutex.withLock { playingTrack = nextTrack }
                play(nextTrack, false)
            } else {
                queue.clear()
                mutex.withLock { playingTrack = null }
            }
        }
    }

    override suspend fun play(track: AudioTrack, queue: Boolean) {
        if(!queue) {
            node.ws.send("play", guild.id) {
                put("track", track.encodedTrack.encodedTrack)
            }
            mutex.withLock { playingTrack = track }
        } else {
            if(!this.queue.isNotEmpty() && playingTrack == null) {
                node.ws.send("play", guild.id) {
                    put("track", track.encodedTrack.encodedTrack)
                }
                mutex.withLock { this.playingTrack = track }
            } else {
                this.queue += track
            }
        }
    }

    override suspend fun stop() = node.ws.send("stop", guild.id)

    override suspend fun pause() = node.ws.send("pause", guild.id) { put("pause", true) }

    override suspend fun resume() = node.ws.send("pause", guild.id) { put("pause", false) }

    override suspend fun skip() = play(queue.removeFirst(), false)

    override suspend fun setEqualizer(bands: List<Pair<Int, Float>>) = node.ws.send("equalizer", guild.id) {
        putJsonArray("bands") {
            bands.forEach { add(buildJsonObject {
                put("band", it.first)
                put("gain", it.second)
            }) }
        }
    }

    override suspend fun setVolume(volume: Int) = node.ws.send("volume", guild.id) { put("volume", volume) }.also {
        mutex.withLock {
            this.volume = volume
        }
    }

}