/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild.member

import com.soywiz.klock.DateTimeTz
import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.PermissionHolder
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.UserCacheEntry
import io.github.jan.discordkm.api.entities.activity.Activity
import io.github.jan.discordkm.api.entities.activity.PresenceStatus
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannel
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannelCacheEntry
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.containers.CacheMemberRoleContainer
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite
import io.github.jan.discordkm.api.entities.guild.VoiceStateCacheEntry
import io.github.jan.discordkm.internal.caching.CacheEntry
import io.github.jan.discordkm.internal.caching.MemberCacheManager
import io.github.jan.discordkm.internal.entities.DiscordImage
import io.github.jan.discordkm.internal.serialization.rawValue
import kotlin.jvm.JvmName

interface MemberCacheEntry : Member, Nameable, PermissionHolder, CacheEntry {

    /*
     * The time at which the member joined the guild
     */
    val joinedAt: DateTimeTz

    /*
     * The time at which the member started boosting the guild
     */
    val premiumSince: DateTimeTz?

    /*
     * Whether the member is deafened
     */
    val isDeafened: Boolean

    /*
     * Whether the member is muted
     */
    val isMuted: Boolean

    /*
     * Whether this member is in the verification process
     */
    val isPending: Boolean

    /*
     * The server specific nickname of the member
     */
    val nickname: String?

    /*
     * The date where the timeout will be removed from the member. (Until then the member can't do anything in this guild)
     */
    val timeoutUntil: DateTimeTz?

    /*
     * The voice state of the member containing the channel etc.
     */
    val voiceState: VoiceStateCacheEntry?

    /*
     * The status of the member or offline if the status couldn't be found
     */
    val status: PresenceStatus

    /*
     * The activities of the member
     */
    val activities: List<Activity>

    /*
     * The roles of the member
     */
    override val roles: CacheMemberRoleContainer

    /*
     * The nickname of the member or the name if [nickname] is null
     */
    override val name: String
        get() = nickname ?: user.cache!!.name

    override val client: DiscordClient
        get() = guild.client

    /*
     * Whether the member is the owner of the guild
     */
    val isOwner: Boolean

    /*
     * The avatar url of the member
     */
    val avatarUrl: String?

    /*
     * Returns all permissions this user has
     */
    override val permissions: Set<Permission>

}

/*
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
internal class MemberCacheEntryImpl(
    override val guild: Guild,
    override val id: Snowflake,
    override val user: UserCacheEntry,
    override val joinedAt: DateTimeTz,
    override val premiumSince: DateTimeTz?,
    override val isDeafened: Boolean,
    override val isMuted: Boolean,
    override val isPending: Boolean,
    override val nickname: String?,
    avatarHash: String?,
    override val timeoutUntil: DateTimeTz?
) : MemberCacheEntry {

    /*
     * The voice state of the member retrieved from cache
     */
    override val voiceState: VoiceStateCacheEntry?
        get() = guild.cache?.voiceStates?.get(id)

    /*
     * The status of the member
     */
    override val status: PresenceStatus
        get() = guild.cache?.presences?.get(id)?.status ?: PresenceStatus.OFFLINE

    /*
     * A list of activities the user currently has
     */
    override val activities: List<Activity>
        get() = guild.cache?.presences?.get(id)?.activities ?: emptyList()

    val cacheManager = MemberCacheManager(client)

    override val roles: CacheMemberRoleContainer
        get() = CacheMemberRoleContainer(this, cacheManager.roleCache.values)

    /*
     * Whether the member is the owner of the guild
     */
    override val isOwner: Boolean
        @get:JvmName("isOwner")
        get() = guild.cache?.owner?.id == id

    /*
     * The avatar url of the member
     */
    override val avatarUrl = avatarHash?.let { DiscordImage.memberAvatar(id, guild.id, it) }

    /*
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

    /*
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