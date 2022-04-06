/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.entities

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Sticker

object DiscordImage {

    private val baseURL = "https://cdn.discordapp.com/"

    fun applicationImage(id: Snowflake, hash: String) = getImage("app-icons", id, hash)

    fun guildBanner(id: Snowflake, hash: String) = getImage("banners", id, hash, if(hash.startsWith("a_")) "gif" else "png")

    fun guildIcon(id: Snowflake, hash: String) = getImage("icons", id, hash, if(hash.startsWith("a_")) "gif" else "png")

    fun guildSplash(id: Snowflake, hash: String) = getImage("splashes", id, hash)

    fun guildDiscoverySplash(id: Snowflake, hash: String) = getImage("discovery-splashes", id, hash)

    fun userBanner(id: Snowflake, hash: String) = getImage("banners", id, hash, if(hash.startsWith("a_")) "gif" else "png")

    fun defaultUserAvatar(discriminator: Int) = "${baseURL}embed/avatars/${discriminator % 5}"

    fun userAvatar(id: Snowflake, hash: String) = getImage("avatars", id, hash, if(hash.startsWith("a_")) "gif" else "png")

    fun sticker(id: Snowflake, type: Sticker.FormatType) = "${baseURL}stickers/$id.${if(type == Sticker.FormatType.PNG || type == Sticker.FormatType.APNG) "png" else "json"}"

    fun roleIcon(id: Snowflake, hash: String) = getImage("role-icons", id, hash)

    fun memberAvatar(id: Snowflake, guildId: Snowflake, hash: String) = "${baseURL}guilds/$guildId/users/$id/avatars/$hash.png"

    fun eventCoverImage(id: Snowflake, hash: String) = getImage("guild-events", id, hash)

    private fun getImage(path: String, id: Snowflake, hash: String, extension: String = "png") = "$baseURL$path/$id/$hash.$extension"

}