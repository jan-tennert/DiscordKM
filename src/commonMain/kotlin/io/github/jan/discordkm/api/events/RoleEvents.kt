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

/**
 * Sent when a role was created
 */
class RoleCreateEvent(override val role: Role) : RoleEvent

/**
 * Sent when a role was updated
 */
class RoleUpdateEvent(override val role: Role, val oldRole: Role?) : RoleEvent

/**
 * Sent when a role was deleted
 */
class RoleDeleteEvent(override val client: Client, val roleId: Snowflake) : Event