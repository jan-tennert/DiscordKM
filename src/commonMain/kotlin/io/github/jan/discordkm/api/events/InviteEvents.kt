package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.invites.Invite
import io.github.jan.discordkm.internal.entities.channels.Invitable

class InviteCreateEvent(val invite: Invite) : Event {

    override val client: Client
        get() = invite.client

}

class InviteDeleteEvent(val channel: Invitable, override val guild: Guild, val inviteCode: String) : GuildEvent