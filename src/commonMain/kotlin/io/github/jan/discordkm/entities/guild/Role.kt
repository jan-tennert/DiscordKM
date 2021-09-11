/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.guild

import io.github.jan.discordkm.entities.Mentionable
import io.github.jan.discordkm.entities.PermissionHolder
import io.github.jan.discordkm.entities.Reference
import io.github.jan.discordkm.entities.SerializableEntity
import io.github.jan.discordkm.entities.SnowflakeEntity
import io.github.jan.discordkm.entities.guild.channels.GuildChannel
import io.github.jan.discordkm.utils.getColor
import io.github.jan.discordkm.utils.getEnums
import io.github.jan.discordkm.utils.getId
import io.github.jan.discordkm.utils.getOrThrow
import io.github.jan.discordkm.utils.getRoleTag
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

class Role(val guild: Guild, override val data: JsonObject) : Mentionable, Reference<Role>, SnowflakeEntity, SerializableEntity, PermissionHolder {

    override val id = data.getId()

    /**
     * Returns the name of the role
     */
    val name = data.getOrThrow<String>("name")

    /**
     * Returns the color of the role
     */
    val color = data.getColor("color")

    /**
     * If this role is pinned in the user listing
     */
    @get:JvmName("isHoist")
    val isHoist = data.getOrThrow<Boolean>("hoist")

    /**
     * The position of this role
     */
    val position = data.getOrThrow<Int>("position")

    /**
     * The permissions which this role has
     */
    override val permissions = data.getEnums("permissions", Permission)

    override fun getPermissionsFor(channel: GuildChannel) = channel.permissionOverrides.first { it.holder is Role && it.holder.id == id }.allow

    /**
     * Whether this role is managed by an interaction
     */
    @get:JvmName("isManaged")
    val isManaged = data.getOrThrow<Boolean>("managed")

    /**
     * Whether this role is mentionable
     */
    @get:JvmName("isMentionable")
    val isMentionable = data.getOrThrow<Boolean>("mentionable")

    /**
     * The tags this role has
     */
    val tags = data.getRoleTag("tags")
    override val client = guild.client

    override val asMention = "<@&$id>"

    override fun getValue(ref: Any?, property: KProperty<*>) = guild.roles[id]!!

    override fun toString() = "Role[name=$name, id=$id]"

    override fun equals(other: Any?): Boolean {
        if(other !is Role) return false
        return other.id == id
    }

    class Tag(val botId: Long? = null, integrationId: Long? = null, val premiumSubscriber: Boolean? = null)
}