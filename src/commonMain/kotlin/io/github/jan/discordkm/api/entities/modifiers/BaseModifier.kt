package io.github.jan.discordkm.api.entities.modifiers

sealed interface BaseModifier <T> {

    val data: T

}