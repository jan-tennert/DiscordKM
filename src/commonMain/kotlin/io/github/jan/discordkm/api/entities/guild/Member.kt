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
import com.soywiz.klock.parse
import io.github.jan.discordkm.api.entities.PermissionHolder
import io.github.jan.discordkm.api.entities.Reference
import io.github.jan.discordkm.api.entities.SerializableEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.channels.GuildChannel
import io.github.jan.discordkm.api.entities.guild.channels.VoiceChannel
import io.github.jan.discordkm.api.entities.lists.RetrievableRoleList
import io.github.jan.discordkm.api.entities.misc.EnumList
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.entities.UserData
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.entities.guilds.MemberData
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractClientEntity
import io.github.jan.discordkm.internal.utils.extractGuildEntity
import io.github.jan.discordkm.internal.utils.getOrDefault
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

interface Member : Reference<Member>, SnowflakeEntity, GuildEntity, PermissionHolder {

    /**
     * Returns the [UserData] of this member
     */
    val user: User
        get() = data.getValue("user").jsonObject.extractClientEntity(guild.client)

    val voiceState: VoiceState?
        get() = guild.voiceStates.firstOrNull { it.userId == id }

    override val client: Client
        get() = guild.client

    override val id: Snowflake
        get() = user.id

    /**
     * Whether the member is the owner of the guild
     */
    val isOwner: Boolean
        @get:JvmName("isOwner")
        get() = (guild.ownerId == id)

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
    val nickname: String
        get() = data.getOrDefault("nick", user.name)

    /**
     * Returns the roles of the member
     */
    val roles: RetrievableRoleList

    /**
     * Returns the date when the member joined his guild
     */
    val joinedAt: DateTimeTz
        get() = ISO8601.DATETIME_UTC_COMPLETE.parse(data.getOrThrow("joined_at"))

    /**
     * Returns the date when the member boosted his guild. Can be null if the user isn't boosting his server
     */
    val premiumSince: DateTimeTz?
        get() = if(data.getOrNull<String>("premium_since") != null) ISO8601.DATETIME_UTC_COMPLETE.parse(data.getOrThrow("premium_since")) else null

    /**
     * Whether the member is deafened
     */
    val isDeafened: Boolean
        @get:JvmName("isDeafened")
        get() = data.getOrThrow<Boolean>("deaf")

    /**
     * Whether the member is muted
     */
    val isMuted: Boolean
        @get:JvmName("isMuted")
        get() = data.getOrThrow<Boolean>("mute")

    /**
     * Whether the member hasn't passed the guilds Membership screen requirements
     */
    val isPending: Boolean
        @get:JvmName("isPending")
        get() = data.getOrDefault("pending", false)

    /**
     * Modifies this user
     */

    suspend fun modify(modifier: MemberModifier.() -> Unit) = client.buildRestAction<Member> {
        route = Route.Member.MODIFY_MEMBER(guild.id, id).patch(MemberModifier().apply(modifier).build())
        transform { it.toJsonObject().extractGuildEntity(guild) }
        onFinish { (guild as GuildData).memberCache[it.id] = it }
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

    //mute etc in voice entity

    override fun getValue(ref: Any?, property: KProperty<*>) = guild.members[id]!!

    override suspend fun retrieve() = guild.members.retrieve(id)

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
     * The time at which the user requested to speak. Only if the user is in a [StageChannel]
     */
    val requestToSpeakTimestamp: DateTimeTz?
        get() = ISO8601.DATETIME_UTC_COMPLETE.tryParse(data.getOrNull<String>("request_to_speak_timestamp") ?: "")

}