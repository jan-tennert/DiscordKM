/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.guild

import com.soywiz.klock.ISO8601
import com.soywiz.klock.parse
import io.github.jan.discordkm.Cache
import io.github.jan.discordkm.entities.PermissionHolder
import io.github.jan.discordkm.entities.Reference
import io.github.jan.discordkm.entities.SerializableEntity
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.SnowflakeEntity
import io.github.jan.discordkm.entities.User
import io.github.jan.discordkm.entities.guild.channels.GuildChannel
import io.github.jan.discordkm.entities.guild.channels.VoiceChannel
import io.github.jan.discordkm.entities.lists.RetrievableRoleList
import io.github.jan.discordkm.entities.misc.EnumList
import io.github.jan.discordkm.restaction.CallsTheAPI
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractClientEntity
import io.github.jan.discordkm.utils.extractGuildEntity
import io.github.jan.discordkm.utils.getOrDefault
import io.github.jan.discordkm.utils.getOrNull
import io.github.jan.discordkm.utils.getOrThrow
import io.github.jan.discordkm.utils.putOptional
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

class Member(val guild: Guild, override val data: JsonObject) : Reference<Member>, SnowflakeEntity, SerializableEntity, PermissionHolder {

    /**
     * Returns the [User] of this member
     */
    var user = data.getValue("user").jsonObject.extractClientEntity<User>(guild.client)
        private set

    override val client = guild.client

    override val id = user.id

    /**
     * Whether the member is the owner of the guild
     */
    @get:JvmName("isOwner")
    val isOwner = (guild.ownerId == id)

    /**
     * Returns all permissions this user has
     */
    override val permissions: EnumList<Permission>
        get() {
            val permissions = roles.map { it.permissions.toList() }.flatten().toMutableList()
            if(isOwner) permissions += Permission.ALL_PERMISSIONS
            return EnumList(Permission, permissions)
        }

    override fun getPermissionsFor(channel: GuildChannel): EnumList<Permission> {
        if(isOwner) return EnumList(Permission, Permission.ALL_PERMISSIONS)
        if(Permission.ADMINISTRATOR in permissions)return EnumList(Permission, Permission.ALL_PERMISSIONS)
        if(channel.permissionOverrides.isEmpty()) return EnumList(Permission, guild.everyoneRole.permissions.toList())

        //*Placeholder*
        val permissions = mutableSetOf<Permission>()
        channel.permissionOverrides.forEach {
            if(it.holder is Role && it.holder in roles) permissions.addAll(it.allow.toList())
            if(it.holder is Member && it.holder.id == id) permissions.addAll(it.allow.toList())
        }
        return EnumList(Permission, permissions.toList())
    }

    /**
     * Returns the nickname of the member. If the member doesn't have a nickname it returns his real name
     */
    val nickname = data.getOrDefault("nick", user.name)

    internal val roleCache = Cache.fromSnowflakeEntityList(data.getValue("roles").jsonArray.map { guild.roles[Snowflake.fromId(it.jsonPrimitive.long)]!! })

    /**
     * Returns the roles of the member
     */
    val roles
        get() = RetrievableRoleList(this, roleCache.values)

    /**
     * Returns the date when the member joined his guild
     */
    val joinedAt = ISO8601.DATETIME_UTC_COMPLETE.parse(data.getOrThrow("joined_at"))

    /**
     * Returns the date when the member boosted his guild. Can be null if the user isn't boosting his server
     */
    val premiumSince = if(data.getOrNull<String>("premium_since") != null) ISO8601.DATETIME_UTC_COMPLETE.parse(data.getOrThrow("premium_since")) else null

    /**
     * Whether the member is deafened
     */
    @get:JvmName("isDeafened")
    val isDeafened = data.getOrThrow<Boolean>("deaf")

    /**
     * Whether the member is muted
     */
    @get:JvmName("isMuted")
    val isMuted = data.getOrThrow<Boolean>("mute")

    /**
     * Whether the member hasn't passed the guilds Membership screen requirements
     */
    val isPending = data.getOrDefault("pending", false)

    /**
     * Modifies this user
     */
    @CallsTheAPI
    suspend fun modify(modifier: MemberModifier.() -> Unit) = client.buildRestAction<Member> {
        action = RestAction.Action.patch("/guilds/${guild.id}/members/$id", MemberModifier().apply(modifier).build())
        transform { it.toJsonObject().extractGuildEntity(guild) }
        onFinish { guild.memberCache[it.id] = it }
    }

    /**
     * Kicks the member from the guild.
     *
     * Requires the permission [Permission.KICK_MEMBERS]
     */
    @CallsTheAPI
    suspend fun kick() = guild.members.kick(id)

    /**
     * Bans a member from the guild
     *
     * Requires the permission [Permission.BAN_MEMBERS]
     */
    suspend fun ban(delDays: Int?) = guild.members.ban(id, delDays)

    //mute etc in voice entity

    override fun getValue(ref: Any?, property: KProperty<*>) = guild.members[id]!!

    override fun toString() = "Member[nickname=$nickname, id=$id]"

    override fun equals(other: Any?): Boolean {
        if(other !is Member) return false
        return other.id == id
    }

}

class MemberModifier {

    var nickname: String? = null
    val roleIds = mutableListOf<Snowflake>()
    var mute: Boolean? = null
    var deaf: Boolean? = null
    private var channelId: Snowflake? = null

    fun role(role: Role) { roleIds += role.id }

    fun moveTo(voiceChannel: VoiceChannel) { channelId = voiceChannel.id }

    fun build() = buildJsonObject {
        putOptional("nickname", nickname)
        putOptional("roles", roleIds.ifEmpty { null })
        putOptional("mute", mute)
        putOptional("deaf", deaf)
        putOptional("channel_id", channelId)
    }

}