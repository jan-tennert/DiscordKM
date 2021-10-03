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
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.events.TypingStartEvent
import io.github.jan.discordkm.internal.entities.channels.MessageChannel
import io.github.jan.discordkm.internal.entities.channels.MessageChannelData
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject

class TypingStartEventHandler(val client: Client) : InternalEventHandler<TypingStartEvent> {

    override fun handle(data: JsonObject): TypingStartEvent {
        val user = client.users[data.getOrThrow<Snowflake>("user_id")]!!
        val guild = client.guilds[data.getOrNull<Snowflake>("guild_id") ?: Snowflake.empty()]
        val member = guild?.members?.get(user.id)
        val channel = (client.channels[data.getOrThrow<Snowflake>("channel_id")] ?: MessageChannelData.fromId(client, data.getOrThrow("channel_id"))) as MessageChannel
        val timestamp = DateTimeTz.Companion.fromUnixLocal(data.getOrThrow<Long>("timestamp"))
        return TypingStartEvent(channel, guild, user, member, timestamp)
    }

}