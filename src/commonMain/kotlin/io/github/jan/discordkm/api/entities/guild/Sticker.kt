/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild

import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.internal.entities.DiscordImage
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter

/**
 * Represents a Sticker. Can be a default sticker or a guild sticker
 * @param packId The id of the pack this sticker belongs to
 * @param id The id of the sticker
 * @param name The name of the sticker
 * @param description The description of the sticker
 * @param tags The tags of the sticker
 * @param formatType The format type of the sticker (PNG, APNG, LOTTIE)
 * @param sortValue The standard sticker's sort order within its pack
 * @param isAvailable Whether the sticker is available. Can be false if the guild server boost tier changed
 * @param type The type of the sticker (DEFAULT, GUILD)
 */
class Sticker(
    val packId: Snowflake?,
    val name: String,
    val description: String?,
    val tags : List<String>,
    val type: StickerType,
    val formatType: FormatType,
    val isAvailable: Boolean,
    val guild: Guild?,
    val sortValue: Int?,
    val creator: User?,
    override val client: Client,
    override val id: Snowflake
) : BaseEntity, SnowflakeEntity {

    val url = DiscordImage.sticker(id, formatType)

    enum class FormatType : EnumWithValue<Int> {
        PNG,
        APNG,
        LOTTIE;

        override val value: Int
            get() = ordinal + 1

        companion object : EnumWithValueGetter<FormatType, Int>(values())
    }

    class Item(val name: String, override val id: Snowflake, val formatType: FormatType) : SnowflakeEntity
}

enum class StickerType : EnumWithValue<Int> {
    STANDARD,
    GUILD;

    override val value: Int
        get() = ordinal + 1

    companion object : EnumWithValueGetter<StickerType, Int>(values())
}