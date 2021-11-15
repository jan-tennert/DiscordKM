/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.channels.guild.GuildChannel
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.invites.Invite
import io.github.jan.discordkm.internal.entities.channels.Invitable

/**
 * Sent when an invite was created
 */
class InviteCreateEvent(val invite: Invite) : Event {

    override val client: Client
        get() = invite.client

}

/**
 * Sent when an invite was deleted
 */
class InviteDeleteEvent(val channel: GuildChannel, override val guild: Guild, val inviteCode: String) : GuildEvent