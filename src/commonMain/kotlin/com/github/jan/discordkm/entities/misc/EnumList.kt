package com.github.jan.discordkm.entities.misc

import com.github.jan.discordkm.entities.EnumSerializer
import com.github.jan.discordkm.entities.SerializableEnum

class EnumList<T : SerializableEnum<T>>(private val serializer: EnumSerializer<T>, private val list: List<T>) : Iterable<T> {

    val rawValue = serializer.encode(list)
    override operator fun iterator() = list.iterator()
    override fun toString() = "EnumList[${list.joinToString()}]"

    companion object {

        fun <T : SerializableEnum<T>> empty() = EnumList<T>(EmptyEnumSerializer(), emptyList())

    }

}

class EmptyEnumSerializer<T : SerializableEnum<T>> : EnumSerializer<T> {
    override fun decode(value: Long) = EnumList(this, emptyList())

    override fun encode(list: List<T>) = 0L

    override val values
        get() = emptyList<T>()
}