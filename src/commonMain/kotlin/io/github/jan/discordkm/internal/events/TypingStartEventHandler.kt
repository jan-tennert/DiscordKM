/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

import com.soywiz.klock.DateTimeTz
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.member.Member
import io.github.jan.discordkm.api.events.TypingStartEvent
import io.github.jan.discordkm.internal.utils.long
import io.github.jan.discordkm.internal.utils.snowflake
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import io.github.jan.discordkm.internal.utils.get

internal class TypingStartEventHandler(val client: Client) : InternalEventHandler<TypingStartEvent> {

    override suspend fun handle(data: JsonObject): TypingStartEvent {
        val user = User(data["user_id"]!!.snowflake, client)
        val guild = data["guild_id", true]?.snowflake?.let { Guild(it, client) }
        val channel = MessageChannel(data["channel_id"]!!.snowflake, client)
        val timestamp = DateTimeTz.fromUnixLocal(data["timestamp"]!!.long)
        val member = data["member"]?.jsonObject?.let { Member(it, guild!!) }
        return TypingStartEvent(channel, guild, user, member, timestamp)
    }

}