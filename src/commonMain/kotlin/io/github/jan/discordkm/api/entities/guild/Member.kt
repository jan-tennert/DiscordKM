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
import com.soywiz.klock.ISO8601
import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.PermissionHolder
import io.github.jan.discordkm.api.entities.Reference
import io.github.jan.discordkm.api.entities.SerializableEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.activity.Activity
import io.github.jan.discordkm.api.entities.activity.PresenceStatus
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.containers.CacheMemberRoleContainer
import io.github.jan.discordkm.api.entities.containers.MemberRoleContainer
import io.github.jan.discordkm.api.entities.guild.channels.GuildChannel
import io.github.jan.discordkm.api.entities.guild.channels.VoiceChannel
import io.github.jan.discordkm.api.entities.misc.FlagList
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.caching.CacheEntity
import io.github.jan.discordkm.internal.caching.CacheEntry
import io.github.jan.discordkm.internal.caching.MemberCacheManager
import io.github.jan.discordkm.internal.entities.DiscordImage
import io.github.jan.discordkm.internal.entities.guilds.MemberData
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.rawValue
import io.github.jan.discordkm.internal.serialization.serializers.MemberSerializer
import io.github.jan.discordkm.internal.utils.extractGuildEntity
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

open class Member protected constructor(override val id: Snowflake, override val guild: Guild) : Reference<Member>, SnowflakeEntity, GuildEntity, CacheEntity {

    override val cache: MemberCacheEntry?
        get() = guild.cache?.members?.get(id)
    open val user: User = User.from(id, client)
    open val roles = MemberRoleContainer(this)

    /**
     * Modifies this user
     */
    suspend fun modify(modifier: MemberModifier.() -> Unit) = client.buildRestAction<Member> {
        route = Route.Member.MODIFY_MEMBER(guild.id, id).patch(MemberModifier().apply(modifier).build())
        transform { it.toJsonObject().extractGuildEntity(guild) }
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
        fun from(id: Snowflake, guild: Guild) = Member(id, guild)
        fun from(data: JsonObject, guild: Guild) = MemberSerializer.deserialize(data, guild)
    }

    override fun getValue(ref: Any?, property: KProperty<*>) = guild.members[id]!!

    override suspend fun retrieve() = guild.members.retrieve(id)

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
    val voiceState: VoiceState?

    /**
     * The status of the member
     */
    val status: PresenceStatus

    /**
     * A list of activities the user currently has
     */
    val activities: List<Activity>

    val cacheManager = MemberCacheManager()

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
            if (isOwner) return Permission.ALL_PERMISSIONS.toSet()
            var permission: Long = guild.cache!!.everyoneRole.cache!!.permissions.rawValue()
            for (role in roles) {
                permission = permission or role.permissions.rawValue()
                if ((permission and Permission.ADMINISTRATOR.rawValue) == Permission.ADMINISTRATOR.rawValue) return Permission.ALL_PERMISSIONS.toSet()
            }
            return Permission.decode(permission)
        }

    /**
     * Returns the permission for the member in a specific guild channel
     * @param channel The guild channel
     * @see GuildChannel
     */
    override fun getPermissionsFor(channel: GuildChannel): FlagList<Permission> {
        if (isOwner) return FlagList(Permission, Permission.ALL_PERMISSIONS)
        if (Permission.ADMINISTRATOR in permissions) return FlagList(Permission, Permission.ALL_PERMISSIONS)
        if (channel.permissionOverrides.isEmpty()) return FlagList(Permission, guild.everyoneRole.permissions.toList())

        //*Placeholder*
        val permissions = mutableSetOf<Permission>()
        channel.permissionOverrides.forEach {
            if (it.holder is Role && it.holder in roles) permissions.addAll(it.allow.toList())
            if (it.holder is Member && it.holder.id == id) permissions.addAll(it.allow.toList())
        }
        return FlagList(Permission, permissions.toList())
    }


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

interface VoiceState : SerializableEntity {

    val guildId: Snowflake?
        get() = data.getOrNull("guild_id")

    val guild: Guild?
        get() = client.guilds[guildId ?: Snowflake.empty()]

    val channelId: Snowflake?
        get() = data.getOrNull("channel_id")

    val channel: VoiceChannel?
        get() = client.channels[channelId ?: Snowflake.empty()] as? VoiceChannel

    val userId: Snowflake
        get() = data.getOrThrow("user_id")

    val member: Member?
        get() = data["member"]?.jsonObject?.let { MemberData(client.guilds[guildId!!]!!, it) }

    val sessionId: String
        get() = data.getOrThrow("session_id")

    val isDeafendbyGuld: Boolean
        get() = data.getOrThrow("deaf")

    val isMutedbyGuild: Boolean
        get() = data.getOrThrow("mute")

    val isMuted: Boolean
        get() = data.getOrThrow("self_mute")

    val isDeafend: Boolean
        get() = data.getOrThrow("self_deaf")

    val isStreaming: Boolean
        get() = data["self_stream"]?.jsonPrimitive?.booleanOrNull ?: false

    val hasCameraEnabled: Boolean
        get() = data.getOrThrow("self_video")

    val isMutedByBot: Boolean
        get() = data.getOrThrow("supress")

    val isInVoiceChannel: Boolean
        get() = channelId != null

    /**
     * The time at which the user requested to speak or was invited to speak. Only if the user is in a [StageChannel]
     */
    val requestToSpeakTimestamp: DateTimeTz?
        get() = ISO8601.DATETIME_UTC_COMPLETE.tryParse(data.getOrNull<String>("request_to_speak_timestamp") ?: "")

    /**
     * Invites this [member] to speak in a [StageChannel]
     */
    suspend fun inviteToSpeak()

    /**
     * Accepts the speak request from this [member] if he has one
     */
    suspend fun acceptSpeakRequest()

    /**
     * Declines the speak request from this [member] if he has one
     */
    suspend fun declineSpeakRequest()

}