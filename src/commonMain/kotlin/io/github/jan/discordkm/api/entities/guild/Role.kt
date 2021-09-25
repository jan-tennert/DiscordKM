/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild

import io.github.jan.discordkm.api.entities.Mentionable
import io.github.jan.discordkm.api.entities.PermissionHolder
import io.github.jan.discordkm.api.entities.Reference
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.guild.channels.GuildChannel
import io.github.jan.discordkm.api.entities.misc.Color
import io.github.jan.discordkm.api.entities.misc.EnumList
import io.github.jan.discordkm.internal.utils.DiscordImage
import io.github.jan.discordkm.internal.utils.getColor
import io.github.jan.discordkm.internal.utils.getEnums
import io.github.jan.discordkm.internal.utils.getId
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.getRoleTag
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.buildJsonObject
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

interface Role : Mentionable, Reference<Role>, SnowflakeEntity, GuildEntity, PermissionHolder {

    override val id: Snowflake
        get() = data.getId()

    /**
     * Returns the name of the role
     */
    val name: String
        get() = data.getOrThrow<String>("name")

    /**
     * Returns the color of the role
     */
    val color: Color
        get() = data.getColor("color")

    /**
     * The icon of the role
     */
    val iconUrl: String?
        get() = data.getOrNull<String?>("icon")?.let { DiscordImage.roleIcon(id, it) }

    /**
     * If this role is pinned in the user listing
     */
    val isHoist: Boolean
        @get:JvmName("isHoist")
        get() = data.getOrThrow<Boolean>("hoist")

    /**
     * The position of this role
     */
    val position: Int
        get() = data.getOrThrow<Int>("position")

    /**
     * The permissions which this role has
     */
    override val permissions: EnumList<Permission>
        get() = data.getEnums("permissions", Permission)

    override fun getPermissionsFor(channel: GuildChannel) = channel.permissionOverrides.first { it.holder is Role && it.holder.id == id }.allow

    /**
     * Whether this role is managed by an interaction
     */
    val isManaged: Boolean
        @get:JvmName("isManaged")
        get() = data.getOrThrow<Boolean>("managed")

    /**
     * Whether this role is mentionable
     */
    val isMentionable: Boolean
        @get:JvmName("isMentionable")
        get() = data.getOrThrow<Boolean>("mentionable")

    /**
     * The tags this role has
     */
    val tags: Tag?
        get() = data.getRoleTag("tags")

    override val client
        get() = guild.client

    override val asMention: String
        get() = "<@&$id>"

    override fun getValue(ref: Any?, property: KProperty<*>) = guild.roles[id]!!

    /**
     * Modifies this role
     *
     * Requires the permission [Permission.MANAGE_ROLES]
     */
    suspend fun modify(modifier: RoleModifier.() -> Unit): Role

    /**
     * Changes the position of the role
     *
     * Requires the permission [Permission.MANAGE_ROLES]
     */

    suspend fun setPosition(newPosition: Int): Role

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