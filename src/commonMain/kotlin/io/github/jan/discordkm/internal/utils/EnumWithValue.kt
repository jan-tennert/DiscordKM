package io.github.jan.discordkm.internal.utils

interface EnumWithValue<V> {

    val value: V

}

open class EnumWithValueGetter <V : EnumWithValue<T>, T>(val values: Collection<V>) {

    constructor(values: Array<V>) : this(values.toList())

    fun from(value: T) = values.first { it.value == value }

}