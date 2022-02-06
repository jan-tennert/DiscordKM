package io.github.jan.discordkm.api.entities.misc

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.orElse
import arrow.core.toOption
import com.soywiz.korio.file.VfsFile
import com.soywiz.korio.file.baseName
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korio.lang.format
import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.DiscordLocale
import io.github.jan.discordkm.internal.utils.string
import io.github.jan.discordkm.internal.utils.toJsonObject

class TranslationManager private constructor(private val rawTranslations: List<TranslationFile>, private val mergeEnglish: Boolean = false, val defaultLanguage: DiscordLocale = DiscordLocale.ENGLISH_US) {

    val translations
        get() = rawTranslations.associateBy(TranslationFile::language)

    operator fun get(locale: DiscordLocale, key: String, vararg args: Any): Option<String> {
        val translation = translations[locale].toOption().orElse { if(locale.isEnglish && mergeEnglish) this.translations[DiscordLocale.ENGLISH_GREAT_BRITAIN].toOption() else None }
        return translation.flatMap { it.translations[key].toOption() }.map { it.format(args) }
    }

    companion object {
        suspend fun fromFolder(folder: VfsFile, mergeEnglish: Boolean = false, defaultLanguage: DiscordLocale = DiscordLocale.ENGLISH_US) : TranslationManager {
            val files = folder.list().let {
                val list = mutableListOf<VfsFile>()
                it.collect { f ->
                    list.add(f)
                }
                list
            }
            return TranslationManager(files.map {
                DiscordLocale.getOption(it.baseName.split(".")[0].replace("_", "-")) to it.readString().toJsonObject().asSequence().associate { i -> i.key to i.value.string }
            }.filter { it.first is Some }.map { TranslationFile((it.first as Some).value, it.second) }, mergeEnglish, defaultLanguage)
        }
        suspend fun fromResources(subFolder: String, mergeEnglish: Boolean = false, defaultLanguage: DiscordLocale = DiscordLocale.ENGLISH_US) = fromFolder(resourcesVfs[subFolder], mergeEnglish, defaultLanguage)
        fun empty() = TranslationManager(emptyList())
    }

}

data class TranslationFile(val language: DiscordLocale, val translations: Map<String, String>)

fun BaseEntity.text(locale: DiscordLocale, key: String, vararg args: Any) = client.config.translationManager.get(locale, key, *args)
//fun InteractionCreateEvent.text(key: String, guildLocale: Boolean = false, vararg args: Any) = client.config.translationManager.get(interaction.locale ?: if(guildLocale) interaction.guildLocale ?: client.config.translationManager.defaultLanguage else client.config.translationManager.defaultLanguage, key, *args)
