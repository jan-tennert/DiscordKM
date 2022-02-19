/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.activity

import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import kotlinx.serialization.Serializable

/**
 * The type for the [Activity]
 */
@Serializable(with = ActivityType.Companion::class)
enum class ActivityType : EnumWithValue<Int> {
    PLAYING,
    STREAMING,
    LISTENING,
    WATCHING,
    /**
     * This activity type is user-only. Bots can't set them
     */
    CUSTOM,
    COMPETING;

    override val value: Int
        get() = ordinal

    companion object : EnumWithValueGetter<ActivityType, Int>(values())
}