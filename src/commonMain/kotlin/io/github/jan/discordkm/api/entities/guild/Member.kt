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
import io.github.jan.discordkm.api.entities.activity.Activity
import io.github.jan.discordkm.api.entities.activity.PresenceStatus
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannel
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannel
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.containers.CacheMemberRoleContainer
import io.github.jan.discordkm.api.entities.containers.MemberRoleContainer
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.caching.CacheEntity
import io.github.jan.discordkm.internal.caching.CacheEntry
import io.github.jan.discordkm.internal.caching.MemberCacheManager
import io.github.jan.discordkm.internal.entities.DiscordImage
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.MemberSerializer
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlin.jvm.JvmName

open class Member protected constructor(override val id: Snowflake, override val guild: Guild) : SnowflakeEntity, GuildEntity, CacheEntity {

    override val cache: MemberCacheEntry?
        get() = guild.cache?.members?.get(id)
    open val user: User
        get() = User(id, client)
    open val roles = MemberRoleContainer(this)

    /**
     * Modifies this user
     */
    suspend fun modify(modifier: MemberModifier.() -> Unit) = client.buildRestAction<Member> {
        route = Route.Member.MODIFY_MEMBER(guild.id, id).patch(MemberModifier().apply(modifier).build())
        transform { Member(it.toJsonObject(), guild) }
    }

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
    suspend fun ban(delDays: Int?) = guild.members.ban(id, delDays)

    companion object {
        operator fun invoke(id: Snowflake, guild: Guild) = Member(id, guild)
        operator fun invoke(data: JsonObject, guild: Guild) = MemberSerializer.deserialize(data, guild)
    }

}

data class MemberCacheEntry(
    override val guild: Guild,
    override val id: Snowflake,
    override val user: User,
    val joinedAt: DateTimeTz,
    val premiumSince: DateTimeTz?,
    val isDeafened: Boolean,
    val isMuted: Boolean,
    val isPending: Boolean,
    val nickname: String?,
    val avatarHash: String?,
) : Member(id, guild), Nameable, PermissionHolder, CacheEntry {

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
        get() = guild.cache?.ownerId == id

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
            TODO()
            /*if (isOwner) return Permission.ALL_PERMISSIONS.toSet()
            var permission: Long = guild.cache!!.everyoneRole.cache!!.permissions.rawValue()
            for (role in roles) {
                permission = permission or role.permissions.rawValue()
                if ((permission and Permission.ADMINISTRATOR.rawValue) == Permission.ADMINISTRATOR.rawValue) return Permission.ALL_PERMISSIONS.toSet()
            }
            return Permission.decode(permission)*/
        }

    /**
     * Returns the permission for the member in a specific guild channel
     * @param channel The guild channel
     * @see GuildChannel
     */
    override fun getPermissionsFor(channel: GuildChannelCacheEntry): Set<Permission> {
        if (isOwner) return Permission.ALL_PERMISSIONS.toSet()
        if (Permission.ADMINISTRATOR in permissions) return Permission.ALL_PERMISSIONS.toSet()
        if (channel.permissionOverwrites.isEmpty()) return guild.cache!!.everyoneRole.cache!!.permissions.toSet()

        //*Placeholder*
        val permissions = mutableSetOf<Permission>()
        channel.permissionOverwrites.forEach {
            if (it.type == PermissionOverwrite.HolderType.ROLE && it.holderId in roles.map { it.id }) permissions.addAll(it.allow.toList())
            if (it.type == PermissionOverwrite.HolderType.MEMBER && it.holderId == id) permissions.addAll(it.allow.toList())
        }
        return permissions.toSet()
    }

    override val type = PermissionOverwrite.HolderType.MEMBER


}

class MemberModifier {

    /**
     * The new nickname of the member
     */
    var nickname: String? = null

    /**
     * A list of role ids the member will get
     */
    val roleIds = mutableListOf<Snowflake>()

    /**
     * Whether the member should be muted
     */
    var mute: Boolean? = null

    /**
     * Whether the member should be deafend
     */
    var deaf: Boolean? = null
    private var channelId: Snowflake? = null

    /**
     * Adds a role to the member
     */
    fun role(role: Role) {
        roleIds += role.id
    }

    /**
     * Moves the member to a voice channel
     */
    fun moveTo(voiceChannel: VoiceChannel) {
        channelId = voiceChannel.id
    }

    fun build() = buildJsonObject {
        putOptional("nickname", nickname)
        putOptional("roles", roleIds.ifEmpty { null })
        putOptional("mute", mute)
        putOptional("deaf", deaf)
        putOptional("channel_id", channelId)
    }

}
