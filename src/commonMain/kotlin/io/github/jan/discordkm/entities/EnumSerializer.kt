package io.github.jan.discordkm.entities

import io.github.jan.discordkm.entities.misc.EnumList

interface EnumSerializer <T : SerializableEnum<T>> {

    val values: List<T>

    fun decode(value: Long) : EnumList<T> {
        if (value == 0L) return EnumList.empty()
        val list = values.filter { (value and it.rawValue) == it.rawValue; }
        return EnumList(this, list)
    }
    fun encode(list: List<T>) : Long {
        var raw: Long = 0
        list.forEach { raw = raw or it.rawValue }
        return raw
    }

}

interface SerializableEnum <T> {

    val offset: Int
    val rawValue: Long
        get() = 1L shl offset

}