package io.github.jan.discordkm.internal

object Check {

    fun checkArgument(message: String, check: () -> Boolean) = if(!check()) throw IllegalArgumentException(message) else Unit

}

fun <T> T?.check(message: String, check: (T) -> Boolean) = if(this != null) Check.checkArgument(message, check = { check(this) }) else null