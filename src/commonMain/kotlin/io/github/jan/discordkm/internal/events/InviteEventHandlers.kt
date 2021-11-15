/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.Channel
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.invites.Invite
import io.github.jan.discordkm.api.events.InviteCreateEvent
import io.github.jan.discordkm.api.events.InviteDeleteEvent
import io.github.jan.discordkm.internal.entities.channels.Invitable
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.snowflake
import kotlinx.serialization.json.JsonObject

class InviteCreateEventHandler(val client: Client) : InternalEventHandler<InviteCreateEvent> {

    override fun handle(data: JsonObject): InviteCreateEvent {
        val invite = Invite(client, data)
        return InviteCreateEvent(invite)
    }

}

class InviteDeleteEventHandler(val client: Client) : InternalEventHandler<InviteDeleteEvent> {

    override fun handle(data: JsonObject): InviteDeleteEvent {
        val channel = Channel(data["channel"]!!.snowflake, ChannelType.UNKNOWN, client)
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val code = data.getOrThrow<String>("code")
        return InviteDeleteEvent(channel, guild, code)
    }

}