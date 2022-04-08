/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild.sticker

import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEvent
import io.github.jan.discordkm.internal.entities.DiscordImage

/*
 * Represents a Sticker. Can be a default sticker or a guild sticker
 */
interface StickerCacheEntry : Sticker, Nameable {

    /*
     * The id of the pack this sticker belongs to
     */
    val packId: Snowflake?

    /*
     * The description of the sticker
     */
    val description: String?

    /*
     * The tags of the sticker
     */
    val tags : Set<String>

    /*
     * The type of the sticker (DEFAULT, GUILD)
     */
    val type: StickerType

    /*
     * The format type of the sticker (PNG, APNG, LOTTIE)
     */
    val formatType: Sticker.FormatType

    /*
     * Whether the sticker is available. Can be false if the guild server boost tier changed, so that the sticker can't be used anymore
     */
    val isAvailable: Boolean

    /*
     * The standard sticker's sort order within its pack
     */
    val sortValue: Int?

    /*
     * The creator of the sticker
     */
    val creator: User?
}

internal class StickerCacheEntryImpl(
    override val packId: Snowflake?,
    override val name: String,
    override val description: String?,
    override val tags: Set<String>,
    override val type: StickerType,
    override val formatType: Sticker.FormatType,
    override val isAvailable: Boolean,
    override val guild: Guild,
    override val sortValue: Int?,
    override val creator: User?,
    override val id: Snowflake
) : StickerCacheEntry {

    val url = DiscordImage.sticker(id, formatType)

    override val client = guild.client

    override fun toString(): String = "StickerCacheEntry(id=$id, guildId=${guild.id}, name=$name, description=$description)"
    override fun hashCode() = id.hashCode()
    override fun equals(other: Any?): Boolean = other is ScheduledEvent && other.id == id && other.guild.id == guild.id

}