package io.github.jan.discordkm.lavalink

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.on
import io.github.jan.discordkm.internal.restaction.RestAction.Companion.put
import io.github.jan.discordkm.lavalink.events.TrackEndEvent
import io.github.jan.discordkm.lavalink.tracks.AudioTrack
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray

class AudioPlayer(val guildId: Snowflake, private val node: LavalinkNode) {

    var volume: Int = 100
        private set
    val queue = Queue()
    var playingTrack: AudioTrack? = null
        private set
    private val mutex = Mutex()

    init {
        node.client.on<TrackEndEvent> {
            if(reason == "REPLACED") return@on
            if(queue.hasNext && listOf("FINISHED", "REPLACED").contains(reason)) {
                val nextTrack = queue.next()
                mutex.withLock { playingTrack = nextTrack }
                play(nextTrack, false)
            } else {
                queue.clear()
                mutex.withLock { playingTrack = null }
            }
        }
    }

    suspend fun play(track: AudioTrack, queue: Boolean = true) {
        if(!queue) {
            node.ws.send("play", guildId) {
                put("track", track.encodedTrack.encodedTrack)
            }
            mutex.withLock { playingTrack = track }
        } else {
            if(!this.queue.hasNext && playingTrack == null) {
                node.ws.send("play", guildId) {
                    put("track", track.encodedTrack.encodedTrack)
                }
                mutex.withLock { this.playingTrack = track }
            } else {
                this.queue += track
            }
        }
    }

    suspend fun stop() = node.ws.send("stop", guildId)

    suspend fun pause() = node.ws.send("pause", guildId) { put("pause", true) }

    suspend fun resume() = node.ws.send("pause", guildId) { put("pause", false) }

    suspend fun skip() = play(queue.next(), false)

    suspend fun setEqualizer(bands: List<Pair<Int, Float>>) = node.ws.send("equalizer", guildId) {
        putJsonArray("bands") {
            bands.forEach { add(buildJsonObject {
                put("band", it.first)
                put("gain", it.second)
            }) }
        }
    }

    suspend fun setVolume(volume: Int) = node.ws.send("volume", guildId) { put("volume", volume) }.also { this@AudioPlayer.volume = volume }

}