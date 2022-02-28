package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.interactions.components.ActionRow
import io.github.jan.discordkm.api.entities.interactions.components.ComponentWithId
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class ComponentContainer<S : ComponentWithId>(val rows: List<ActionRow>) : List<S> by rows.flatMap(ActionRow::components).map({ it as S }) {

    inline operator fun <reified T : S> get(id: String) = (this.firstOrNull { it.customId == id } as? T) ?: throw IllegalArgumentException("No component with id $id and type ${T::class.simpleName} found")

    inline operator fun <reified T : S> getValue(thisRef: Any?, property: KProperty<*>) = get<T>(property.name)

}