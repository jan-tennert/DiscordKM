/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.lavalink

import co.touchlab.stately.collections.IsoMutableMap
import com.soywiz.klock.DateTime
import com.soywiz.klock.milliseconds
import com.soywiz.klogger.Logger
import com.soywiz.korio.async.launch
import com.soywiz.korio.net.http.Http
import com.soywiz.korio.net.http.createHttpClient
import com.soywiz.korio.net.ws.WebSocketClient
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannel
import io.github.jan.discordkm.api.entities.clients.WSDiscordClient
import io.github.jan.discordkm.api.entities.clients.awaitEvent
import io.github.jan.discordkm.api.entities.clients.on
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.events.VoiceServerUpdate
import io.github.jan.discordkm.api.events.VoiceStateUpdateEvent
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.utils.LoggerOutput
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.toJsonObject
import io.github.jan.discordkm.lavalink.events.LavalinkStatsUpdateEvent
import io.github.jan.discordkm.lavalink.events.TrackEndEvent
import io.github.jan.discordkm.lavalink.events.TrackExceptionEvent
import io.github.jan.discordkm.lavalink.events.TrackStartEvent
import io.github.jan.discordkm.lavalink.events.TrackStuckEvent
import io.github.jan.discordkm.lavalink.stats.LavalinkStats
import io.github.jan.discordkm.lavalink.tracks.AudioPlaylist
import io.github.jan.discordkm.lavalink.tracks.AudioTrack
import io.github.jan.discordkm.lavalink.tracks.AudioTrackImpl
import io.github.jan.discordkm.lavalink.tracks.EncodedTrack
import io.github.jan.discordkm.lavalink.tracks.LoadType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

sealed interface LavalinkNode {

    val shardId: Int
    val client: WSDiscordClient
    val isConnected: Boolean
    val stats: LavalinkStats

    /*
     * Joins [channel] and creates a new [AudioPlayer] for it
     */
    suspend fun join(channel: VoiceChannel): AudioPlayer

    /*
     * Load tracks for the given [identifier]
     */
    suspend fun loadTracks(identifier: String): List<AudioTrack>

    /*
     * Connects to this node
     */
    suspend fun connect()

    /*
     * Disconnects from this node
     */
    suspend fun disconnect()

    /*
     * Returns a AudioPlayer for the given [Guild], if it exists
     */
    fun getPlayerByGuild(guild: Guild) = getPlayerByGuild(guild.id)

    /**
     * Returns a AudioPlayer for the given [guildId], if it exists
     */
    fun getPlayerByGuild(guildId: Snowflake): AudioPlayer?

}

internal class LavalinkNodeImpl(private val ip: String, private val port: Int, private val password: String, override val shardId: Int = 0, override val client: WSDiscordClient): LavalinkNode {

    lateinit var ws: WebSocketClient
    private val http = createHttpClient()
    private val baseUrl = "http://$ip:$port"
    private val audioPlayers = IsoMutableMap<Snowflake, AudioPlayer>()
    private val LOGGER = Logger("LavalinkNode-$shardId")
    override var isConnected = false
    override lateinit var stats: LavalinkStats

    init {
        LOGGER.output = LoggerOutput
        client.on<VoiceServerUpdate> {
            ws.send("voiceUpdate", data.getOrThrow("guild_id")) {
                put("sessionId", this@LavalinkNodeImpl.client.shardConnections[shardId]!!.sessionId)
                put("event", data)
            }
        }
        client.on<VoiceStateUpdateEvent> {
            if(voiceState.user.id == client.selfUser.id) {
                updateVoiceState(this)
            }
        }
        client.on<TrackStartEvent> {
            player.playingTrack?.let { track -> (track as AudioTrackImpl).setPosition(DateTime.now()) }
        }
    }

    private suspend fun updateVoiceState(event: VoiceStateUpdateEvent) {
        if(event.voiceState.channel?.id == null) {
            ws.send("destroy", event.voiceState.guild.id)
            audioPlayers.remove(event.voiceState.guild.id)
        } else {
            if(!audioPlayers.contains(event.voiceState.guild.id)) {
                audioPlayers[event.voiceState.guild.id] = AudioPlayerImpl(event.voiceState.guild, this)
            }
        }
    }

    override suspend fun join(channel: VoiceChannel) : AudioPlayer {
        channel.join()
        val event = client.awaitEvent<VoiceStateUpdateEvent> {
            it.voiceState.user.id == client.selfUser.id && it.voiceState.channel?.id == channel.id
        }
        updateVoiceState(event)
        return audioPlayers[channel.guild.id]!!
    }

    override suspend fun loadTracks(identifier: String) : List<AudioTrack> {
        val request = request(Http.Method.GET, LavalinkRoute.LOAD_TRACK(identifier)).toJsonObject()
        val tracks = buildList<AudioTrack> {
            when(val loadType = LoadType.valueOf(request.getOrThrow("loadType"))) {
                LoadType.SEARCH_RESULT -> TODO()
                LoadType.NO_MATCHES -> throw IllegalArgumentException("No track found for identifier $identifier")
                LoadType.LOAD_FAILED -> throw IllegalArgumentException("Error while loading $identifier: ${request.getValue("exception").jsonObject.getOrThrow<String>("message")}")
                else -> {
                    addAll(request.getValue("tracks").jsonArray.map { json -> AudioTrackImpl(json.jsonObject, this@LavalinkNodeImpl) })
                    if(loadType == LoadType.PLAYLIST_LOADED) {
                        val playlistInfo = request.getValue("playlistInfo").jsonObject
                        val playlistName = playlistInfo.getOrThrow<String>("name")
                        val selectedTrack = playlistInfo.getOrThrow<Int>("selectedTrack")
                        forEach { (it as AudioTrackImpl).setPlaylist(AudioPlaylist(playlistName, getOrNull(selectedTrack))) }
                    }
                }
            }
        }

        return tracks.toList()
    }

    override suspend fun connect() = coroutineScope {
        LOGGER.info { "Connecting to the lavalink server..." }
        try {
            ws = WebSocketClient("ws://$ip:$port", headers = Http.Headers.build {
                put("Authorization", password)
                put("Num-Shards", shardId.toString())
                put("User-Id", client.selfUser.id.string)
            })
            isConnected = true
            LOGGER.info { "Connected to the lavalink server!" }
            ws.onStringMessage {
                onMessage(it)
            }
            Unit
        } catch(err: Exception) {
            isConnected = false
            LOGGER.error { "Error on websocket: $err" }
            throw err
        }
    }

    override suspend fun disconnect() {
        ws.close()
        isConnected = false
    }

    override fun getPlayerByGuild(guildId: Snowflake) = audioPlayers.get(guildId)

    private fun onMessage(message: String) {
        val data = message.toJsonObject()
        if(data.getOrThrow<String>("op") == "stats") {
            val stats = Json { ignoreUnknownKeys = true }.decodeFromJsonElement<LavalinkStats>(data)
            launch(Dispatchers.Default) {
                client.handleEvent(LavalinkStatsUpdateEvent(client, this, stats))
            }
            this.stats = stats
            return
        }
        if(data.getOrThrow<String>("op") != "event") return
        val type = data.getOrThrow<String>("type")
        val track = EncodedTrack(data.getOrNull<String>("track") ?: return, this)
        val player = getPlayerByGuild(data.getOrThrow<Snowflake>("guildId"))!!
        launch(Dispatchers.Default) {
            (client).handleEvent(
                when(type) {
                    "TrackStartEvent" -> TrackStartEvent(client, track, player)
                    "TrackEndEvent" -> TrackEndEvent(client, track, player, data.getOrThrow("reason"))
                    "TrackExceptionEvent" -> TrackExceptionEvent(client, track, player, Throwable(data.getOrThrow<String>("error")))
                    "TrackStuckEvent" -> TrackStuckEvent(client, track, player, data.getOrThrow<Long>("thresholdMs").milliseconds)
                    else -> throw IllegalStateException("Unsupported event: $message")
                }
            )
        }
    }

    suspend fun request(method: Http.Method, endpoint: String) = http.request(method, baseUrl + endpoint, headers = Http.Headers.build {
        put("Authorization", password)
    }).readAllString()
}

internal val LavalinkNode.ws: WebSocketClient
    get() = (this as LavalinkNodeImpl).ws