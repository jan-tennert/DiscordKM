/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.modifier.guild

import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.misc.Color
import io.github.jan.discordkm.api.entities.modifier.JsonModifier
import io.github.jan.discordkm.api.media.Image
import io.github.jan.discordkm.internal.serialization.rawValue
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

class RoleModifier : JsonModifier {

    var name: String? = null
    var permissions: MutableSet<Permission> = mutableSetOf()
    var color: Color? = null
    var hoist: Boolean? = null
    var mentionable: Boolean? = null
    var icon: Image? = null

    override val data: JsonObject
        get() = buildJsonObject {
            putOptional("name", name)
            putOptional("permissions", permissions.rawValue())
            putOptional("color", color?.rgb)
            putOptional("hoist", hoist)
            putOptional("icon", icon?.encodedData)
            putOptional("mentionable", mentionable)
        }

}
