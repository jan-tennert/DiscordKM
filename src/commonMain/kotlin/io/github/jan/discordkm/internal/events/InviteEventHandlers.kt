package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.invites.Invite
import io.github.jan.discordkm.api.events.InviteCreateEvent
import io.github.jan.discordkm.api.events.InviteDeleteEvent
import io.github.jan.discordkm.internal.entities.channels.Invitable
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject

class InviteCreateEventHandler(val client: Client) : InternalEventHandler<InviteCreateEvent> {

    override fun handle(data: JsonObject): InviteCreateEvent {
        val invite = Invite(client, data)
        //maybe cache invites
        return InviteCreateEvent(invite)
    }

}

class InviteDeleteEventHandler(val client: Client) : InternalEventHandler<InviteDeleteEvent> {

    override fun handle(data: JsonObject): InviteDeleteEvent {
        val channel = client.channels[data.getOrThrow<Snowflake>("channel_id")]!! as Invitable
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")]!!
        val code = data.getOrThrow<String>("code")
        return InviteDeleteEvent(channel, guild, code)
    }

}