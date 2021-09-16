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
import io.github.jan.discordkm.entities.misc.Color
import io.github.jan.discordkm.entities.misc.EnumList
import io.github.jan.discordkm.exceptions.PermissionException
import io.github.jan.discordkm.restaction.CallsTheAPI
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.getColor
import io.github.jan.discordkm.utils.getEnums
import io.github.jan.discordkm.utils.getId
import io.github.jan.discordkm.utils.getOrThrow
import io.github.jan.discordkm.utils.getRoleTag
import io.github.jan.discordkm.utils.putOptional
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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

    /**
     * Modifies this role
     *
     * Requires the permission [Permission.MANAGE_ROLES]
     */
    @CallsTheAPI
    suspend fun modify(modifier: RoleModifier.() -> Unit) = client.buildRestAction<Role> {
        action = RestAction.Action.patch("/guilds/${guild.id}/roles/${id}", RoleModifier().apply(modifier).build())
        transform { Role(guild, it.toJsonObject()) }
        onFinish { guild.roleCache[it.id] = it }
        check { if(Permission.MANAGE_ROLES !in guild.selfMember.permissions) throw PermissionException("You need the permission MANAGE_ROLES to modify a role") }
    }

    /**
     * Changes the position of the role
     *
     * Requires the permission [Permission.MANAGE_ROLES]
     */
    @CallsTheAPI
    suspend fun setPosition(newPosition: Int) = client.buildRestAction<Role> {
        action = RestAction.Action.patch("/guilds/${guild.id}/roles", buildJsonArray {
            addJsonObject {
                put("id", id.long)
                put("position", newPosition)
            }
        })
        transform { Role(guild, it.toJsonObject()) }
        onFinish { guild.roleCache[it.id] = it }
        check { if(Permission.MANAGE_ROLES !in guild.selfMember.permissions) throw PermissionException("You need the permission MANAGE_ROLES to modify the role position") }
    }

    class Tag(val botId: Long? = null, integrationId: Long? = null, val premiumSubscriber: Boolean? = null)

    override suspend fun retrieve() = guild.roles.retrieveRoles().first { it.id == id }
}

class RoleModifier {

    var name: String? = null
    var permissions: MutableList<Permission> = mutableListOf()
    var color: Color? = null
    var hoist: Boolean? = null
    var mentionable: Boolean? = null

    fun build() = buildJsonObject {
        putOptional("name", name)
        putOptional("permissions", EnumList(Permission, permissions).rawValue)
        putOptional("color", color)
        putOptional("hoist", hoist)
        putOptional("mentionable", mentionable)
    }

}