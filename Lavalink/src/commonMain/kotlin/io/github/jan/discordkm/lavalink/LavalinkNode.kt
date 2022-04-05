package io.github.jan.discordkm.lavalink

import co.touchlab.stately.collections.IsoMutableMap
import com.soywiz.klock.DateTime
import com.soywiz.klock.milliseconds
import com.soywiz.klogger.Logger
import com.soywiz.korio.net.http.Http
import com.soywiz.korio.net.http.createHttpClient
import com.soywiz.korio.net.ws.WebSocketClient
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannel
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
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
import io.github.jan.discordkm.lavalink.tracks.AudioTrackData
import io.github.jan.discordkm.lavalink.tracks.EncodedTrack
import io.github.jan.discordkm.lavalink.tracks.LoadType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

class LavalinkNode internal constructor(private val ip: String, private val port: Int, private val password: String, val shardId: Int = 0, val client: DiscordWebSocketClient) {

    internal lateinit var ws: WebSocketClient
    private val httpClient = createHttpClient()
    private val baseUrl = "http://$ip:$port"
    private val audioPlayers = IsoMutableMap<Snowflake, AudioPlayer>()
    private val LOGGER = Logger("LavalinkNode-$shardId")
    val players: Map<Snowflake, AudioPlayer>
        get() = audioPlayers.access { it.toMap() }
    var isConnected = false
        private set
    lateinit var stats: LavalinkStats
        private set

    init {
        LOGGER.output = LoggerOutput
        client.on<VoiceServerUpdate> {
            ws.send("voiceUpdate", data.getOrThrow("guild_id")) {
                put("sessionId", this@LavalinkNode.client.shardConnections.first { it.shardId == shardId }.sessionId)
                put("event", data)
            }
        }
        client.on<VoiceStateUpdateEvent> {
            if(voiceState.user.id == client.selfUser.id) {
                updateVoiceState(this)
            }
        }
        client.on<TrackStartEvent> {
            player.playingTrack?.let { track -> (track as AudioTrackData).setPosition(DateTime.now()) }
        }
    }

    private suspend fun updateVoiceState(event: VoiceStateUpdateEvent) {
        if(event.voiceState.channel?.id == null) {
            ws.send("destroy", event.voiceState.guild.id)
            audioPlayers.remove(event.voiceState.guild.id)
        } else {
            if(!audioPlayers.contains(event.voiceState.guild.id)) {
                audioPlayers[event.voiceState.guild.id] = AudioPlayer(event.voiceState.guild.id, this@LavalinkNode)
            }
        }
    }

    suspend fun join(channel: VoiceChannel) : AudioPlayer {
        channel.join()
        val event = client.awaitEvent<VoiceStateUpdateEvent> { it.voiceState.user.id == client.selfUser.id }
        updateVoiceState(event)
        return audioPlayers[channel.guild.id]!!
    }

    suspend fun loadTracks(identifier: String) : List<AudioTrack> {
        val request = request(Http.Method.GET, LavalinkRoute.LOAD_TRACK(identifier)).readAllString().toJsonObject()
        val tracks = mutableListOf<AudioTrack>()
        when(val loadType = LoadType.valueOf(request.getOrThrow("loadType"))) {
            LoadType.SEARCH_RESULT -> TODO()
            LoadType.NO_MATCHES -> throw IllegalArgumentException("No track found for identifier $identifier")
            LoadType.LOAD_FAILED -> throw IllegalArgumentException("Error while loading $identifier: ${request.getValue("exception").jsonObject.getOrThrow<String>("message")}")
            else -> {
                tracks.addAll(request.getValue("tracks").jsonArray.map { json -> AudioTrackData(json.jsonObject, this@LavalinkNode) })
                if(loadType == LoadType.PLAYLIST_LOADED) {
                    val playlistInfo = request.getValue("playlistInfo").jsonObject
                    val playlistName = playlistInfo.getOrThrow<String>("name")
                    val selectedTrack = playlistInfo.getOrThrow<Int>("selectedTrack")
                    tracks.forEach { (it as AudioTrackData).playlist = AudioPlaylist(playlistName, tracks.getOrNull(selectedTrack)) }
                }
            }
        }
        return tracks.toList()
    }

    suspend fun connect() = coroutineScope {
        LOGGER.info { "Connecting to the lavalink server..." }
        launch {
            try {
                ws = WebSocketClient("ws://$ip:$port", headers = Http.Headers.build {
                    put("Authorization", password)
                    put("Num-Shards", shardId.toString())
                    put("User-Id", client.selfUser.id.string)
                })
                isConnected = true
                LOGGER.info { "Connected to the lavalink server!" }
                while (isConnected) {
                    ws.onStringMessage {
                        com.soywiz.korio.async.launch(Dispatchers.Default) {
                            onMessage(it)
                        }
                    }
                }
            } catch(err: Exception) {
                isConnected = false
                LOGGER.error { "Error on websocket: $err" }
                throw err
            }
        }
    }

    suspend fun disconnect() {
        ws.close()
        isConnected = false
    }

    private suspend fun onMessage(message: String) {
        val data = message.toJsonObject()
        if(data.getOrThrow<String>("op") == "stats") {
            val stats = Json { ignoreUnknownKeys = true }.decodeFromJsonElement<LavalinkStats>(data)
            client.handleEvent(LavalinkStatsUpdateEvent(client, this, stats))
            this.stats = stats
            return
        }
        if(data.getOrThrow<String>("op") != "event") return
        val type = data.getOrThrow<String>("type")
        val track = EncodedTrack(data.getOrNull<String>("track") ?: return, this)
        val player = players[data.getOrThrow<Snowflake>("guildId")]!!
        client.handleEvent(
            when(type) {
                "TrackStartEvent" -> TrackStartEvent(client, track, player)
                "TrackEndEvent" -> TrackEndEvent(client, track, player, data.getOrThrow("reason"))
                "TrackExceptionEvent" -> TrackExceptionEvent(client, track, player, Throwable(data.getOrThrow<String>("error")))
                "TrackStuckEvent" -> TrackStuckEvent(client, track, player, data.getOrThrow<Long>("thresholdMs").milliseconds)
                else -> throw IllegalStateException("Unsupported event: $message")
            }
        )
    }

    internal suspend fun request(method: Http.Method, endpoint: String) = httpClient.request(url = baseUrl + endpoint, method = method, headers = Http.Headers.build {
        put("Authorization", password)
    })

}