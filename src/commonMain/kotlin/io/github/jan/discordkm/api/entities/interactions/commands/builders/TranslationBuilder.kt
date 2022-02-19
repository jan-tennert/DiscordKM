package io.github.jan.discordkm.api.entities.interactions.commands.builders

import io.github.jan.discordkm.api.entities.DiscordLocale
import io.github.jan.discordkm.api.entities.interactions.commands.CommandBuilder

class TranslationBuilder {

    val name = mutableMapOf<DiscordLocale, String>()
    val description = mutableMapOf<DiscordLocale, String>()

    @CommandBuilder
    operator fun MutableMap<DiscordLocale, String>.invoke(builder: MutableMap<DiscordLocale, String>.() -> Unit) {
        this.builder()
    }

    @CommandBuilder
    operator fun invoke(builder: TranslationBuilder.() -> Unit) {
        this.builder()
    }

}