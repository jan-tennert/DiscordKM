package io.github.jan.discordkm.lavalink

import io.github.jan.discordkm.lavalink.tracks.AudioTrack
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Queue : Iterable<AudioTrack> {

    private val list = mutableListOf<AudioTrack>()
    private val mutex = Mutex()
    val size: Int
        get() = list.size
    val hasNext: Boolean
        get() = list.isNotEmpty()


    private suspend fun <T> modifyList(modifier: MutableList<AudioTrack>.() -> T) = mutex.withLock { list.modifier() }

    override fun iterator() = list.iterator()

    suspend fun clear() = modifyList { clear() }

    suspend fun add(track: AudioTrack) = modifyList { add(track); }

    suspend operator fun plusAssign(track: AudioTrack) { add(track) }

    suspend fun remove(index: Int) = modifyList { list.removeAt(index) }

    suspend fun next() = mutex.withLock { list.removeFirst() }

}