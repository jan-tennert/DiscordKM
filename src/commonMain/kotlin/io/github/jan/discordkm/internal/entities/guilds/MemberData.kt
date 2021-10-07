/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.entities.guilds

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.ISO8601
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.activity.Activity
import io.github.jan.discordkm.api.entities.activity.PresenceStatus
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.api.entities.guild.VoiceState
import io.github.jan.discordkm.api.entities.lists.RetrievableRoleList
import io.github.jan.discordkm.internal.EntityCache
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.buildRestAction
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put

class MemberData(override val guild: Guild, override val data: JsonObject, val presenceData: JsonObject? = null) : Member {

    val roleCache = EntityCache.fromSnowflakeEntityList(data.getValue("roles").jsonArray.map { guild.roles[Snowflake.fromId(it.jsonPrimitive.long)]!! })

    override val activities: List<Activity>
        get() = guild.presences[id]?.activities ?: emptyList()

    override val status: PresenceStatus
        get() = guild.presences[id]?.status ?: PresenceStatus.OFFLINE

    override val voiceState: VoiceState?
        get() = guild.voiceStates.firstOrNull { it.userId == id }

    override val roles: RetrievableRoleList
        get() = RetrievableRoleList(this, roleCache.values.associateBy { it.id })

    override fun toString() = "Member[nickname=$nickname, id=$id]"

    override fun equals(other: Any?): Boolean {
        if(other !is Member) return false
        return other.id == id
    }

}

class VoiceStateData(override val client: Client, override val data: JsonObject) : VoiceState {

    private val isSelf = userId == client.selfUser.id

    override suspend fun inviteToSpeak() = client.buildRestAction<Unit> {
        route = Route.Voice.MODIFY_VOICE_STATE(guildId!!, userId).patch(buildJsonObject {
            put("channel_id", channelId?.string)
            put("suppress", false)
            put("request_to_speak_timestamp", ISO8601.DATETIME_UTC_COMPLETE.format(DateTimeTz.nowLocal()))
        })
        transform {  }
    }

    override suspend fun acceptSpeakRequest() = client.buildRestAction<Unit> {
        route = Route.Voice.MODIFY_VOICE_STATE(guildId!!, if(isSelf) "@me" else userId).patch(buildJsonObject {
            put("channel_id", channelId?.string)
            put("suppress", false)
        })
        transform {  }
    }

    override suspend fun declineSpeakRequest() = client.buildRestAction<Unit> {
        route = Route.Voice.MODIFY_VOICE_STATE(guildId!!, if(isSelf) "@me" else userId).patch(buildJsonObject {
            put("channel_id", channelId?.string)
            put("suppress", true)
        })
        transform {  }
    }

}