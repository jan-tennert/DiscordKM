package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.interactions.Interaction
import io.github.jan.discordkm.api.entities.interactions.InteractionOption

class OptionList(private val raw: List<InteractionOption>) : Iterable<InteractionOption> {

    operator fun get(name: String) = raw.first { it.name == name }

    operator fun get(position: Int) = raw[position]

    override fun iterator() = raw.iterator()

}