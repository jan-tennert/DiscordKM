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
import io.github.jan.discordkm.api.entities.UserCacheEntry
import io.github.jan.discordkm.api.entities.clients.Intent
import io.github.jan.discordkm.api.entities.guild.Guild

sealed interface BanEvent : GuildEvent {

    val user: User

}

/**
 * Sent when a guild member gets banned
 *
 * Requires the intent [Intent.GUILD_BANS]
 */
class GuildBanAddEvent(override val guild: Guild, override val user: UserCacheEntry) :
    BanEvent

/**
 * Sent when a user gets unbanned
 *
 * Requires the intent [Intent.GUILD_BANS]
 */
class GuildBanRemoveEvent(override val guild: Guild, override val user: UserCacheEntry) :
    BanEvent