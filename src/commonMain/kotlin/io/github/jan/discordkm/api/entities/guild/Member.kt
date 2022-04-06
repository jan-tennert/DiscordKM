/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild

import com.soywiz.klock.DateTimeTz
import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.PermissionHolder
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.UserCacheEntry
import io.github.jan.discordkm.api.entities.activity.Activity
import io.github.jan.discordkm.api.entities.activity.PresenceStatus
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannel
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannel
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.containers.CacheMemberRoleContainer
import io.github.jan.discordkm.api.entities.containers.MemberRoleContainer
import io.github.jan.discordkm.api.entities.modifiers.Modifiable
import io.github.jan.discordkm.api.entities.modifiers.guild.MemberModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.caching.CacheEntity
import io.github.jan.discordkm.internal.caching.CacheEntry
import io.github.jan.discordkm.internal.caching.MemberCacheManager
import io.github.jan.discordkm.internal.entities.DiscordImage
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.rawValue
import io.github.jan.discordkm.internal.serialization.serializers.MemberSerializer
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmName


interface Member : SnowflakeEntity, GuildEntity, CacheEntity, Modifiable<MemberModifier, Member> {

    override val cache: MemberCacheEntry?
        get() = guild.cache?.members?.get(id)
    val user: User
        get() = User(id, client)
    val roles: MemberRoleContainer
        get() = MemberRoleContainer(this)

    /**
     * Modifies this user
     */
    override suspend fun modify(reason: String?, modifier: MemberModifier.() -> Unit) = client.buildRestAction<Member> {
        route = Route.Member.MODIFY_MEMBER(guild.id, id).patch(MemberModifier().apply(modifier).data)
        transform { Member(it.toJsonObject(), guild) }
        this.reason = reason
    }

    /**
     * Time-outs this member
     * @param reason The reason which will be displayed in audit logs
     * @param time The time when the time-out will remove
     */
    suspend fun timeoutUntil(time: DateTimeTz, reason: String? = null) = modify(reason) { timeoutUntil = time }

    /**
     * Modifies the member's nickname
     * @param reason The reason which will be displayed in audit logs
     * @param nickname The new nickname, or null to reset
     */
    suspend fun modifyNickname(nickname: String?, reason: String? = null) = modify(reason) { this.nickname = nickname }

    /**
     * Mutes this member server-wide
     */
    suspend fun mute() = modify { this.mute = true }

    /**
     * Unmutes this member server-wide
     */
    suspend fun unmute() = modify { this.mute = false }

    /**
     * Deafens this member server-wide
     */
    suspend fun deafen() = modify { this.deaf = true }

    /**
     * Undeafens this member server-wide
     */
    suspend fun undeafen() = modify { this.deaf = false }

    /**
     * Moves this member to the specified voice channel. Only works if he is in a voice channel
     */
    suspend fun moveTo(voiceChannel: VoiceChannel) = modify { moveTo(voiceChannel) }

    /**
     * Kicks the member from the guild.
     *
     * Requires the permission [Permission.KICK_MEMBERS]
     */
    suspend fun kick() = guild.members.kick(id)

    /**
     * Bans a member from the guild
     *
     * Requires the permission [Permission.BAN_MEMBERS]
     */
    suspend fun ban(delDays: Int?) = guild.members.ban(id, null, delDays)

    companion object {
        operator fun invoke(id: Snowflake, guild: Guild): Member = MemberImpl(id, guild)
        operator fun invoke(data: JsonObject, guild: Guild) = MemberSerializer.deserialize(data, guild)
    }

}

internal class MemberImpl(override val id: Snowflake, override val guild: Guild) : Member {

    override fun toString(): String = "Member(id=$id, guildId=${guild.id})"
    override fun hashCode() = id.hashCode()
    override fun equals(other: Any?): Boolean = other is Member && other.id == id && other.guild.id == guild.id

}

/**
 * Represents a member of a guild
 * @param id The id of the member
 * @param guild The guild this member belongs to
 * @param user The user this member represents
 * @param joinedAt The date this member joined the guild
 * @param nickname The nickname of this member
 * @param isDeafened Whether this member is deafened
 * @param isMuted Whether this member is muted
 * @param isPending Whether this member is in the verification process
 * @param timeoutUntil The date where the timeout will be removed from the member. (Until then the member can't do anything in this guild)
 */
data class MemberCacheEntry(
    override val guild: Guild,
    override val id: Snowflake,
    override val user: UserCacheEntry,
    val joinedAt: DateTimeTz,
    val premiumSince: DateTimeTz?,
    val isDeafened: Boolean,
    val isMuted: Boolean,
    val isPending: Boolean,
    val nickname: String?,
    val avatarHash: String?,
    val timeoutUntil: DateTimeTz?
) : Member, Nameable, PermissionHolder, CacheEntry {

    /**
     * The voice state of the member retrieved from cache
     */
    val voiceState: VoiceStateCacheEntry?
        get() = guild.cache?.voiceStates?.get(id)

    /**
     * The status of the member
     */
    val status: PresenceStatus
        get() = guild.cache?.presences?.get(id)?.status ?: PresenceStatus.OFFLINE

    /**
     * A list of activities the user currently has
     */
    val activities: List<Activity>
        get() = guild.cache?.presences?.get(id)?.activities ?: emptyList()

    val cacheManager = MemberCacheManager(client)

    override val roles: CacheMemberRoleContainer
        get() = CacheMemberRoleContainer(this, cacheManager.roleCache.values)

    override val name: String
        get() = nickname ?: user.cache!!.name

    override val client: Client
        get() = guild.client

    /**
     * Whether the member is the owner of the guild
     */
    val isOwner: Boolean
        @get:JvmName("isOwner")
        get() = guild.cache?.owner?.id == id

    /**
     * The avatar url of the member
     */
    val avatarUrl: String?
        get() = avatarHash?.let { DiscordImage.memberAvatar(id, guild.id, it) }

    /**
     * Returns all permissions this user has
     */
    override val permissions: Set<Permission>
        get() {
            if(isOwner) return Permission.ALL_PERMISSIONS
            if(guild.cache == null) return setOf()
            val publicRole = guild.cache!!.publicRole
            var basePermissions = publicRole.permissions.rawValue()

            for (role in roles) {
                if(role.cache == null) return setOf()
                basePermissions = basePermissions or role.cache!!.permissions.rawValue()
            }

            val permissions = Permission.decode(basePermissions)
            if(Permission.ADMINISTRATOR in permissions) return Permission.ALL_PERMISSIONS
            return permissions
        }

    /**
     * Returns the permission for the member in a specific guild channel
     * @param channel The guild channel
     * @see GuildChannel
     */
    override fun getPermissionsFor(channel: GuildChannelCacheEntry): Set<Permission> {
        if(Permission.ADMINISTRATOR in permissions) return Permission.ALL_PERMISSIONS

        var basePermissions = permissions.rawValue()
        val publicOverwrite = channel.permissionOverwrites.firstOrNull { it.holderId == guild.id }
        publicOverwrite?.let {
            basePermissions = basePermissions and it.deny.rawValue()
            basePermissions = basePermissions or it.allow.rawValue()
        }

        var allow = 0L
        var deny = 0L

        for (role in roles) {
            val overwrite = channel.permissionOverwrites.firstOrNull { it.holderId == role.id }
            overwrite?.let {
                allow = allow and it.allow.rawValue()
                deny = deny and it.deny.rawValue()
            }
        }

        basePermissions = basePermissions and deny
        basePermissions = basePermissions or allow

        val memberOverwrite = channel.permissionOverwrites.firstOrNull { it.holderId == id }
        memberOverwrite?.let {
            basePermissions = basePermissions and it.deny.rawValue()
            basePermissions = basePermissions or it.allow.rawValue()
        }

        return Permission.decode(basePermissions)
    }

    override val type = PermissionOverwrite.HolderType.MEMBER

    override fun toString(): String = "MemberCacheEntry(id=$id, guildId=${guild.id}, name=$name)"
    override fun hashCode() = id.hashCode()
    override fun equals(other: Any?): Boolean = other is Member && other.id == id && other.guild.id == guild.id

}