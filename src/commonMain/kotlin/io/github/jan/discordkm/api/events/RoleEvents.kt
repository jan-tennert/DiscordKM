package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Role

interface RoleEvent : GuildEvent {

    val role: Role
    override val guild: Guild
        get() = role.guild

}

class RoleCreateEvent(override val role: Role) : RoleEvent

class RoleUpdateEvent(override val role: Role) : RoleEvent

class RoleDeleteEvent(override val client: Client, val roleId: Snowflake) : Event