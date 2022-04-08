/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.internal.serialization.rawValue
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.long
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/*
 * A permission override is used for channels, roles and members. They say what a role/member can do in a specific channel
 * @param holder Can be a [Role] or a [Member]
 * @param allow A list of Permissions the permission override will allow the [holder]
 * @param deny A list of Permission the permission override will deny the [holder]
 */
class PermissionOverwrite(
    val holderId: Snowflake,
    val allow: MutableSet<Permission> = mutableSetOf(),
    val deny: MutableSet<Permission> = mutableSetOf(),
    val type: HolderType
) {

    fun toJsonObject() = buildJsonObject {
        put("id", holderId.long)
        put("allow", allow.rawValue())
        put(
            "type", when (type) {
                HolderType.ROLE -> 0
                HolderType.MEMBER -> 1
            }
        )
        put("deny", deny.rawValue())
    }

    companion object {
        operator fun invoke(data: JsonObject) = PermissionOverwrite(
            Snowflake(data["id"]!!.long),
            Permission.decode(data["allow"]!!.long).toMutableSet(),
            Permission.decode(data["deny"]!!.long).toMutableSet(),
            HolderType.get(data["type"]!!.int)
        )
    }

    enum class HolderType : EnumWithValue<Int> {
        ROLE,
        MEMBER;

        override val value: Int
            get() = ordinal

        companion object : EnumWithValueGetter<HolderType, Int>(values())
    }

}