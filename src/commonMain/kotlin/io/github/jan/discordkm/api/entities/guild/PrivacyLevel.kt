package io.github.jan.discordkm.api.entities.guild

import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter

enum class PrivacyLevel : EnumWithValue<Int> {
    PUBLIC,
    GUILD_ONLY;

    override val value: Int
        get() = ordinal + 1

    companion object : EnumWithValueGetter<PrivacyLevel, Int>(values())
}