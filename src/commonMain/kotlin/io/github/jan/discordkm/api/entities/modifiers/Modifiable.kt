package io.github.jan.discordkm.api.entities.modifiers

interface Modifiable<M : BaseModifier, T> {

    suspend fun modify(modifier: M.() -> Unit): T

}