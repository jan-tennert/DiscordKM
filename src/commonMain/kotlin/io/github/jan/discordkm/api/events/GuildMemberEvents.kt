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