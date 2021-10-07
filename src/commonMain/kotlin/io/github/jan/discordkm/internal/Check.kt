package io.github.jan.discordkm.internal

object Check {

    val SLOWMODE = 0..21600

}

fun <T> T?.check(message: String, check: (T) -> Boolean) = if(this != null) if(!check(this)) throw IllegalArgumentException(message) else Unit else null

fun Int?.checkRange(name: String, range: IntRange) = check("$name has to be between ${range.first} and ${range.last}") { this in range }