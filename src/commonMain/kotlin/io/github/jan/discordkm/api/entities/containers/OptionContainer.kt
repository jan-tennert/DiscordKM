package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.interactions.InteractionOption
import kotlin.reflect.KProperty

class OptionContainer(private val raw: List<InteractionOption>) : Iterable<InteractionOption> {

    operator fun get(name: String) = getOrNull(name) ?: throw IllegalArgumentException("Option with name $name does not exist")

    operator fun get(position: Int) = getOrNull(position) ?: throw IllegalArgumentException("Option on position $position does not exist")

    fun getOrNull(name: String) = raw.firstOrNull { it.name == name }

    fun getOrNull(position: Int) = raw.getOrNull(position)

    override fun iterator() = raw.iterator()

    inline operator fun <reified T> getValue(thisRef: Any?, property: KProperty<*>) = get(property.name).let {
        (it.value as? T) ?: throw IllegalArgumentException("Option with name ${property.name} is not of type ${T::class.simpleName}")
    }

}