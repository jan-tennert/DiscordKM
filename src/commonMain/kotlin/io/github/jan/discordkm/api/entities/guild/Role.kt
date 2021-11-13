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
import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.PermissionHolder
import io.github.jan.discordkm.api.entities.Reference
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannel
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannelCacheEntry
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.channels.GuildChannel
import io.github.jan.discordkm.api.entities.guild.channels.PermissionOverwrite
import io.github.jan.discordkm.api.entities.misc.Color
import io.github.jan.discordkm.internal.serialization.rawValue
import io.github.jan.discordkm.api.media.Image
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.caching.CacheEntity
import io.github.jan.discordkm.internal.caching.CacheEntry
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.RoleSerializer
import io.github.jan.discordkm.internal.serialization.serializers.UserSerializer
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlin.reflect.KProperty

open class Role protected constructor(override val id: Snowflake, override val guild: Guild) : Mentionable, Reference<Role>, SnowflakeEntity, GuildEntity, CacheEntity {

    override val client
        get() = guild.client

    override val asMention: String
        get() = "<@&$id>"

    override val cache: RoleCacheEntry?
        get() = guild.cache?.cacheManager?.roleCache?.get(id)

    override fun getValue(ref: Any?, property: KProperty<*>) = guild.roles[id]!!

    suspend fun modify(modifier: RoleModifier.() -> Unit) = client.buildRestAction<RoleCacheEntry> {
        route = Route.Role.MODIFY_ROLE(guild.id, id).patch(RoleModifier().apply(modifier).build())
        transform { RoleSerializer.deserialize(it.toJsonObject(), guild) }
    }

    override fun toString() = "Role[id=$id]"

    override suspend fun retrieve() = guild.roles.retrieveRoles().first { it.id == id }

    override fun fromCache() = guild.cache?.cacheManager?.roleCache?.get(id)

    companion object {
        fun from(id: Snowflake, guild: Guild) = Role(id, guild)
        fun from(data: JsonObject, guild: Guild) = RoleSerializer.deserialize(data, guild)
    }
}

/**
 * A role cache entry contains all information given by the Discord API
 * @param guild the guild this role belongs to
 * @param id the id of this role
 * @param name the name of this role
 * @param color the color of this role
 * @param isHoist whether this role is hoisted
 * @param position the position of this role
 * @param permissions the permissions of this role
 * @param isManagedByAnIntegration whether this role is managed
 * @param tags The tags this role has
 * @param unicodeEmoji The unicode emoji this role has
 * @param iconHash The icon hash of the role
 */
data class RoleCacheEntry(
    override val id: Snowflake,
    override val permissions: Set<Permission>,
    override val guild: Guild,
    override val name: String,
    val color: Color,
    val isHoist: Boolean,
    val iconHash: String?,
    val unicodeEmoji: String?,
    val position: Int,
    val isManagedByAnIntegration: Boolean,
    val isMentionable: Boolean,
    val tags: Tag?,
) : Role(id, guild), PermissionHolder, Nameable, CacheEntry {

    override val client: Client = guild.client

    data class Tag(
        @SerialName("bot_id") val botId: Snowflake? = null,
        @SerialName("integration_id") val integrationId: Snowflake? = null,
        @SerialName("premium_subscriber") val premiumSubscriber: Boolean? = null
    )

    override fun getPermissionsFor(channel: GuildChannelCacheEntry) = channel.permissionOverwrites.first { it.type == PermissionOverwrite.HolderType.ROLE && it.holderId == id }.allow

    override fun toString() = "Role[id=$id, name=$name]"

}

class RoleModifier {

    var name: String? = null
    var permissions: MutableSet<Permission> = mutableSetOf()
    var color: Color? = null
    var hoist: Boolean? = null
    var mentionable: Boolean? = null
    var icon: Image? = null

    fun build() = buildJsonObject {
        putOptional("name", name)
        putOptional("permissions", permissions.rawValue())
        putOptional("color", color?.rgb)
        putOptional("hoist", hoist)
        putOptional("icon", icon?.encodedData)
        putOptional("mentionable", mentionable)
    }

}