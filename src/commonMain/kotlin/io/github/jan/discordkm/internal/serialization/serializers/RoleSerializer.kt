/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.serialization.serializers

import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.guild.role.RoleCacheEntry
import io.github.jan.discordkm.api.entities.guild.role.RoleCacheEntryImpl
import io.github.jan.discordkm.api.entities.misc.Color
import io.github.jan.discordkm.internal.serialization.GuildEntitySerializer
import io.github.jan.discordkm.internal.utils.boolean
import io.github.jan.discordkm.internal.utils.get
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.long
import io.github.jan.discordkm.internal.utils.snowflake
import io.github.jan.discordkm.internal.utils.string
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

internal object RoleSerializer : GuildEntitySerializer<RoleCacheEntry> {

    override fun deserialize(data: JsonObject, value: Guild): RoleCacheEntry = RoleCacheEntryImpl(
        id = data["id"]!!.snowflake,
        name = data["name"]!!.string,
        color = Color(data["color"]!!.int),
        isHoist = data["hoist"]!!.boolean,
        position = data["position"]!!.int,
        permissions = Permission.decode(data["position"]!!.long),
        isManagedByAnIntegration = data["managed"]!!.boolean,
        isMentionable = data["mentionable"]!!.boolean,
        guild = value,
        iconHash = data["icon", true]?.string,
        unicodeEmoji = data["unicode_emoji", true]?.string?.let { Emoji.fromUnicode(it) },
        tags = data["tags"]?.let { Json.decodeFromJsonElement(it) },
    )

}