/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.Intent
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Member

interface MemberEvent : Event {

    val member: Member
    override val client: Client
        get() = member.client

}

/**
 * Sent when a user joins a guild
 *
 * Requires the intent [Intent.GUILD_MEMBERS]
 */
class GuildMemberAddEvent(override val member: Member) : MemberEvent

/**
 * Sent when a member gets updated
 *
 * Requires the intent [Intent.GUILD_MEMBERS]
 */
class GuildMemberUpdateEvent(override val member: Member) : MemberEvent

/**
 * Sent when a member leaves his guild
 *
 * Requires the intent [Intent.GUILD_MEMBERS]
 */
class GuildMemberRemoveEvent(override val guild: Guild, val user: User) : GuildEvent