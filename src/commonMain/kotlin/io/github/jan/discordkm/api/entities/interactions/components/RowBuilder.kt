package io.github.jan.discordkm.api.entities.interactions.components

class RowBuilder(val components: MutableList<Component> = mutableListOf()) {

    fun build() = ActionRow(components)

}

class ActionRowBuilder(val rows: MutableList<ActionRow> = mutableListOf()) {

    fun row(builder: RowBuilder.() -> Unit) { rows += RowBuilder().apply(builder).build() }

    fun add(row: ActionRow) { rows += row }

}