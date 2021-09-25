package io.github.jan.discordkm.api.entities.lists

import io.github.jan.discordkm.api.entities.interactions.Interaction

class OptionList(private val raw: List<Interaction.InteractionOption>) : Iterable<Interaction.InteractionOption> {

    operator fun get(name: String) = raw.first { it.name == name }

    operator fun get(position: Int) = raw[position]

    override fun iterator() = raw.iterator()

}