package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.interactions.InteractionOption
import kotlin.reflect.KProperty

class OptionContainer(private val raw: List<InteractionOption>) : Iterable<InteractionOption> {

    operator fun get(name: String) = getOrNull(name) ?: throw IllegalArgumentException("Option with name $name does not exist")

    inline operator fun <reified T> get(name: String) : T = get(name) as T

    inline operator fun <reified T> get(name: String, default: T) : T = (getOrNull(name)?.value as? T) ?: default

    operator fun get(position: Int) = getOrNull(position) ?: throw IllegalArgumentException("Option on position $position does not exist")

    inline operator fun <reified T> get(position: Int) : T = get(position) as T

    inline operator fun <reified T> get(position: Int, default: T) : T = (getOrNull(position)?.value as? T) ?: default

    fun getOrNull(name: String) = raw.firstOrNull { it.name == name }

    fun getOrNull(position: Int) = raw.getOrNull(position)

    override fun iterator() = raw.iterator()

    inline operator fun <reified T> getValue(thisRef: Any?, property: KProperty<*>) = get(property.name).let {
        (it.value as? T) ?: throw IllegalArgumentException("Option with name ${property.name} is not of type ${T::class.simpleName}")
    }

}