package io.github.jan.discordkm.lavalink.tracks

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.toJsonObject
import io.github.jan.discordkm.lavalink.LavalinkNode
import io.github.jan.discordkm.lavalink.LavalinkRoute
import io.ktor.http.HttpMethod
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

sealed interface AudioTrack {

    val identifier: String
    val isSeekable: Boolean
    val author: String
    val length: TimeSpan
    val isStream: Boolean
    val position: TimeSpan
    val title: String
    val url: String
    val type: AudioTrackType
    val encodedTrack: EncodedTrack
    val playlist: AudioPlaylist?
    val node: LavalinkNode
    val remainingTime: TimeSpan

    enum class AudioTrackType {
        YOUTUBE,
        SOUNDCLOUD,
        BANDCAMP,
        VIMEO,
        TWITCH,
        BEAM,
        FILE,
        HTTP_URL
    }

}

class AudioPlaylist(val name: String, val selectedTrack: AudioTrack?)

class AudioTrackData(data: JsonObject, override val node: LavalinkNode) : AudioTrack {

    private val mutex = Mutex()
    private val info = data.getValue("info").jsonObject
    private var startTime = DateTime.now()
    override var playlist: AudioPlaylist? = null
    override val author = info.getOrThrow<String>("author")
    override val isSeekable = info.getOrThrow<Boolean>("isSeekable")
    override val length = info.getOrThrow<Int>("length").milliseconds
    override val isStream = info.getOrThrow<Boolean>("isStream")
    override val position: TimeSpan
        get() {
            val time = DateTime.now() - startTime
            if(time == TimeSpan.ZERO || time < 0.seconds) return 0.seconds
            return time
        }
    override val title = info.getOrThrow<String>("title")
    override val url = info.getOrThrow<String>("uri")
    override val identifier = info.getOrThrow<String>("identifier")
    override val remainingTime: TimeSpan
        get() = if(position > length) TimeSpan.ZERO else length - position
    override val type: AudioTrack.AudioTrackType
        get() = when {
            url.contains("youtube") || url.contains("youtu.be") -> AudioTrack.AudioTrackType.YOUTUBE
            url.contains("twitch") -> AudioTrack.AudioTrackType.TWITCH
            url.contains("soundcloud") -> AudioTrack.AudioTrackType.SOUNDCLOUD
            else -> AudioTrack.AudioTrackType.HTTP_URL
        }
    override val encodedTrack = EncodedTrack(data.getOrThrow<String>("track"), node)

    suspend fun setPosition(startTime: DateTime) {
        mutex.withLock { this.startTime = startTime }
    }

}

data class EncodedTrack(val encodedTrack: String, private val node: LavalinkNode) {

    suspend fun decode(): AudioTrack {
        val track = node.request(HttpMethod.Get, LavalinkRoute.DECODE_TRACK(encodedTrack))
        return AudioTrackData(buildJsonObject {
            put("track", encodedTrack)
            put("info", track.toJsonObject())
        }, node)
    }

}