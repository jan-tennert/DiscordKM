package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.interactions.InteractionOption
import kotlin.reflect.KProperty

class OptionContainer(val raw: List<InteractionOption>) : Iterable<InteractionOption> {

    inline operator fun <reified T> get(name: String) : T = getOrNull(name)  ?: throw IllegalArgumentException("Option with name $name does not exist")

    inline operator fun <reified T> get(name: String, default: T) : T = getOrNull(name) ?: default

    inline operator fun <reified T> get(position: Int) = getOrNull<T>(position) ?: throw IllegalArgumentException("Option on position $position does not exist")

    inline operator fun <reified T> get(position: Int, default: T) : T = getOrNull(position) ?: default

    inline fun <reified T> getOrNull(name: String) = raw.firstOrNull { it.name == name }?.let {
        when(T::class) {
            InteractionOption::class -> it as? T
            else -> it.value as? T
        }
    }

    inline fun <reified T> getOrNull(position: Int) = raw.getOrNull(position)?.let {
        when(T::class) {
            InteractionOption::class -> it as? T
            else -> (it.value as? T)
        }
    }

    override fun iterator() = raw.iterator()

    inline operator fun <reified T> getValue(thisRef: Any?, property: KProperty<*>) = get<T>(property.name)

}