/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild.invites

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.ISO8601
import io.github.jan.discordkm.api.entities.SerializableEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.ISO8601Serializer
import io.github.jan.discordkm.internal.utils.extractClientEntity
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.valueOfIndexOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.jvm.JvmName

/**
 * An invite is used to easily let a user join a guild, voice channel, stage instance, event etc.
 */
class Invite(override val client: Client, override val data: JsonObject) : SerializableEntity {

    /**
     * The code of the [Invite]
     */
    val code = data.getOrThrow<String>("code")

    /**
     * The invite link of the invite. Looks like this:
     *
     * https://discord.gg/[code]
     */
    val url = "https://discord.gg/$code"

    /**
     * The guild, if it is an invite to a guild channel
     */
    val guild = client.guilds[data["guild"]?.jsonObject?.getOrThrow<Snowflake>("id") ?: Snowflake.empty()]

    /**
     * The channel this invite is for
     */
    val channel = guild?.channels?.get(data["guild"]?.jsonObject?.getOrThrow<Snowflake>("id") ?: Snowflake.empty())

    /**
     * The inviter who created this invite
     */
    val inviter = data["inviter"]?.jsonObject?.extractClientEntity<User>(client)

    /**
     * The type of the invite
     */
    val type = valueOfIndexOrNull<TargetType>(data.getOrNull<Int>("type"))

    /**
     * The target user for this invite, if this an invite to a stream
     */
    val targetUser = data["target_user"]?.jsonObject?.extractClientEntity<User>(client)

    //TODO: add remaining parameters
    val application = data["target_application"]?.jsonObject?.extractClientEntity<InviteApplication>(client)

    /**
     * The invite expiration date
     */
    val expiresAt = ISO8601.DATETIME_UTC_COMPLETE.tryParse(data.getOrNull<String>("expires_at") ?: "")

    /**
     * [Metadata] for this invite. It is always null except when you retrieve it
     */
    val metadata = if(data.getOrNull<Int>("uses") != null) Json {
        ignoreUnknownKeys = true
    }.decodeFromString<Metadata>(data.toString()) else null

    /**
     * Deletes this invite
     */
    suspend fun delete() = client.buildRestAction<Unit> {
        route = Route.Invite.DELETE_INVITE(code).delete()
        transform {  }
    }

    //stage instance object

    //approximate counts etc.

    @Serializable
    data class Metadata(
        val uses: Int,
        @SerialName("max_uses")
        val maxUses: Int,
        @SerialName("max_age")
        val maxAge: Int,
        @SerialName("temporary")
        @get:JvmName("isTemporary")
        val isTemporary: Boolean,
        @Serializable(with = ISO8601Serializer::class)
        @SerialName("created_at")
        val createdAt: DateTimeTz
    )

    enum class TargetType {
        STREAM {
            override fun create(targetId: Snowflake) = Target(targetId, STREAM)
        },
        EMBEDDED_APPLICATION {
            override fun create(targetId: Snowflake) = Target(targetId, EMBEDDED_APPLICATION)
        };

        abstract fun create(targetId: Snowflake) : Target
    }

}

