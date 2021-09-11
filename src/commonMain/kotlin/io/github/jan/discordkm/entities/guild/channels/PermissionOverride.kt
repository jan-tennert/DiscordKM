/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.guild.channels

import io.github.jan.discordkm.entities.PermissionHolder
import io.github.jan.discordkm.entities.guild.Permission
import io.github.jan.discordkm.entities.misc.EnumList
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class PermissionOverride(val holder: PermissionHolder, val allow: EnumList<Permission> = EnumList.empty(), val deny: EnumList<Permission> = EnumList.empty()) {

    fun toJsonObject() = buildJsonObject {
        put("holder", holder.id.long)
        put("allow", allow.rawValue)
        put("deny", deny.rawValue)
    }

}