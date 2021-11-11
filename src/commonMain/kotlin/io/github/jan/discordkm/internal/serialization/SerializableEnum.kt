package io.github.jan.discordkm.internal.serialization

interface SerializableEnum <T> {

    val offset: Int
    val rawValue: Long
        get() = 1L shl offset

}