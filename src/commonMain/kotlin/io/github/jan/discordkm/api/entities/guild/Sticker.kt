/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild

import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.modifiers.guild.StickerModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.entities.DiscordImage
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.GuildSerializer
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import io.github.jan.discordkm.internal.utils.toJsonObject

sealed interface Sticker : GuildEntity, SnowflakeEntity {

    /**
     * Modifies this sticker
     */
    suspend fun modify(builder: StickerModifier.() -> Unit) = client.buildRestAction<Sticker> {
        route = Route.Sticker.MODIFY_GUILD_STICKER(guild.id, id).patch(StickerModifier(true).apply(builder).data)
        transform { GuildSerializer.deserializeSticker(it.toJsonObject(), guild) }
    }

    /**
     * Deletes this sticker
     */
    suspend fun delete() = client.buildRestAction<Unit> {
        route = Route.Sticker.DELETE_GUILD_STICKER(guild.id, id).delete()
    }

    enum class FormatType : EnumWithValue<Int> {
        PNG,
        APNG,
        LOTTIE;

        override val value: Int
            get() = ordinal + 1

        companion object : EnumWithValueGetter<FormatType, Int>(values())
    }

    companion object {

        operator fun invoke(id: Snowflake, guild: Guild): Sticker = IndependentSticker(id, guild)

    }

}

data class IndependentSticker(override val id: Snowflake, override val guild: Guild) : Sticker

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
class StickerCacheEntry(
    val packId: Snowflake?,
    override val name: String,
    val description: String?,
    val tags : List<String>,
    val type: StickerType,
    val formatType: Sticker.FormatType,
    val isAvailable: Boolean,
    override val guild: Guild,
    val sortValue: Int?,
    val creator: User?,
    override val id: Snowflake
) : Sticker, Nameable {

    val url = DiscordImage.sticker(id, formatType)

    override val client = guild.client

}

data class StickerItem(val name: String, override val id: Snowflake, val formatType: Sticker.FormatType) : SnowflakeEntity

enum class StickerType : EnumWithValue<Int> {
    STANDARD,
    GUILD;

    override val value: Int
        get() = ordinal + 1

    companion object : EnumWithValueGetter<StickerType, Int>(values())
}