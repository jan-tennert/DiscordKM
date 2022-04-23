/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.lavalink.tracks

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.korio.net.http.Http
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.toJsonObject
import io.github.jan.discordkm.lavalink.LavalinkNode
import io.github.jan.discordkm.lavalink.LavalinkNodeImpl
import io.github.jan.discordkm.lavalink.LavalinkRoute
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

data class AudioPlaylist(val name: String, val selectedTrack: AudioTrack?)

internal class AudioTrackImpl(data: JsonObject, override val node: LavalinkNode) : AudioTrack {

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

    suspend fun setPlaylist(playlist: AudioPlaylist) {
        mutex.withLock { this.playlist = playlist }
    }

}

data class EncodedTrack(val encodedTrack: String, private val node: LavalinkNode) {

    suspend fun decode(): AudioTrack {
        val track = (node as LavalinkNodeImpl).request(Http.Method.GET, LavalinkRoute.DECODE_TRACK(encodedTrack))
        return AudioTrackImpl(buildJsonObject {
            put("track", encodedTrack)
            put("info", track.toJsonObject())
        }, node)
    }

}