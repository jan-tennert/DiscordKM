package io.github.jan.discordkm

interface DiscordEnum <T, E : Enum<E>> {

    operator fun get(key: T): E

}