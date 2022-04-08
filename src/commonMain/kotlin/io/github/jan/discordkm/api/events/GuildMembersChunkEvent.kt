/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.member.MemberCacheEntry

/*
 * This event is sent when the bot is requesting a chunk of members from the guild.
 * @param guild The guild that the chunk is for.
 * @param members The members in the chunk.
 * @param chunkIndex The index of the chunk.
 * @param chunkCount The total number of chunks.
 * @param notFoundUsers The users that were specified in the request but not found on the server.
 * @param presences The presences of the members in the chunk (if enabled).
 */
class GuildMembersChunkEvent(val guild: Guild, val members: List<MemberCacheEntry>, val chunkIndex: Int, val chunkCount: Int, val notFoundUsers: List<User>, val presences: List<Guild.GuildPresenceCacheEntry>) : Event {

    override val client = guild.client

}