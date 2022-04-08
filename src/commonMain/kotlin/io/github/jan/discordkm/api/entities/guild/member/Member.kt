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
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.channels.guild.VoiceChannel
import io.github.jan.discordkm.api.entities.containers.MemberRoleContainer
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.GuildEntity
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.modifiers.Modifiable
import io.github.jan.discordkm.api.entities.modifiers.guild.MemberModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.caching.CacheEntity
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.MemberSerializer
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject


interface Member : SnowflakeEntity, GuildEntity, CacheEntity, Modifiable<MemberModifier, Member> {

    override val cache: MemberCacheEntry?
        get() = guild.cache?.members?.get(id)
    val user: User
        get() = User(id, client)
    val roles: MemberRoleContainer
        get() = MemberRoleContainer(this)

    /*
     * Modifies this user
     */
    override suspend fun modify(reason: String?, modifier: MemberModifier.() -> Unit) = client.buildRestAction<Member> {
        route = Route.Member.MODIFY_MEMBER(guild.id, id).patch(MemberModifier().apply(modifier).data)
        transform { Member(it.toJsonObject(), guild) }
        this.reason = reason
    }

    /*
     * Time-outs this member
     * @param reason The reason which will be displayed in audit logs
     * @param time The time when the time-out will remove
     */
    suspend fun timeoutUntil(time: DateTimeTz, reason: String? = null) = modify(reason) { timeoutUntil = time }

    /*
     * Modifies the member's nickname
     * @param reason The reason which will be displayed in audit logs
     * @param nickname The new nickname, or null to reset
     */
    suspend fun modifyNickname(nickname: String?, reason: String? = null) = modify(reason) { this.nickname = nickname }

    /*
     * Mutes this member server-wide
     */
    suspend fun mute() = modify { this.mute = true }

    /*
     * Unmutes this member server-wide
     */
    suspend fun unmute() = modify { this.mute = false }

    /*
     * Deafens this member server-wide
     */
    suspend fun deafen() = modify { this.deaf = true }

    /*
     * Undeafens this member server-wide
     */
    suspend fun undeafen() = modify { this.deaf = false }

    /*
     * Moves this member to the specified voice channel. Only works if he is in a voice channel
     */
    suspend fun moveTo(voiceChannel: VoiceChannel) = modify { moveTo(voiceChannel) }

    /*
     * Kicks the member from the guild.
     *
     * Requires the permission [Permission.KICK_MEMBERS]
     */
    suspend fun kick() = guild.members.kick(id)

    /*
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