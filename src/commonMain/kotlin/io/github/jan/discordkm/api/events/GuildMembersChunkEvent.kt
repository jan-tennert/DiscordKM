package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.member.MemberCacheEntry

/**
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