/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild.channels.modifier

import io.github.jan.discordkm.api.entities.PermissionHolder
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.guild.channels.Category
import io.github.jan.discordkm.api.entities.guild.channels.GuildChannel
import io.github.jan.discordkm.api.entities.guild.channels.PermissionOverride
import io.github.jan.discordkm.api.entities.misc.EnumList
import io.github.jan.discordkm.internal.utils.putJsonObject
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray

sealed interface GuildChannelModifier <C : GuildChannel> {
    var name: String?
    val position: Int?
    val permissionOverrides: MutableList<PermissionOverride>

    fun addPermissionOverride(holder: PermissionHolder, allow: Set<Permission> = emptySet(), deny: Set<Permission> = emptySet()) {
        permissionOverrides += PermissionOverride(holder, EnumList(Permission, allow.toList()), EnumList(Permission, deny.toList()))
    }

    fun build() = buildJsonObject {
        putOptional("name", name)
        putOptional("position", position)
        putJsonArray("permission_overrides") {
            permissionOverrides.forEach {
                add(it.toJsonObject())
            }
        }
    }

}

sealed interface NonCategoryModifier <C : GuildChannel> : GuildChannelModifier<C> {

    var parentId: Snowflake?

    fun setParent(id: Snowflake) { parentId = id }

    fun setParent(category: Category) = setParent(category.id)

    override fun build() = buildJsonObject {
        putOptional("parent_id", parentId)
        putJsonObject(super.build())
    }

}

interface GuildChannelBuilder<out C: GuildChannel, out M : GuildChannelModifier<out C>> {

    fun create(builder: M.() -> Unit) : JsonObject

}