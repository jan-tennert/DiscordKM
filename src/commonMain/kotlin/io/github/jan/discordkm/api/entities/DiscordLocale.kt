package io.github.jan.discordkm.api.entities

import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter

enum class DiscordLocale(override val value: String) : EnumWithValue<String> {
    ENGLISH_US("en-US"),
    ENGLISH_GREAT_BRITAIN("en-GB"),
    BULGARIAN("bg"),
    CHINESE_CHINA("zh-CN"),
    CHINESE_TAIWAN("zh-TW"),
    CROATIAN("hr"),
    CZECH("cs"),
    DANISH("da"),
    DUTCH("nl"),
    FINNISH("fi"),
    FRENCH("fr"),
    GERMAN("de"),
    GREEK("el"),
    HINDI("hi "),
    HUNGARIAN("hu"),
    ITALIAN("it"),
    JAPANESE("ja"),
    KOREAN("ko"),
    LITHUANIAN("lt"),
    NORWEGIAN("no"),
    POLISH("pl"),
    PORTUGUESE_BRAZIL("pt-BR"),
    ROMANIAN("ro"),
    RUSSIAN("ru"),
    SPANISH_SPAIN("es-ES"),
    SWEDISH("sv-SE"),
    THAI("th"),
    TURKISH("tr"),
    UKRAINIAN("uk"),
    VIETNAMESE("vi");

    val isEnglish get() = this == ENGLISH_US || this == ENGLISH_GREAT_BRITAIN

    companion object : EnumWithValueGetter<DiscordLocale, String>(values())
}