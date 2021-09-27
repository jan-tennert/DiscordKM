package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Member

interface MemberEvent : Event {

    val member: Member
    override val client: Client
        get() = member.client

}

class GuildMemberAddEvent(override val member: Member) : MemberEvent
class GuildMemberUpdateEvent(override val member: Member) : MemberEvent
class GuildMemberRemoveEvent(override val guild: Guild, val user: User) : GuildEvent