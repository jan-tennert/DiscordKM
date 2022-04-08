/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
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