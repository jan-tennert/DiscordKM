package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.api.events.RoleCreateEvent
import io.github.jan.discordkm.api.events.RoleDeleteEvent
import io.github.jan.discordkm.api.events.RoleUpdateEvent
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.entities.guilds.RoleData
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class RoleCreateEventHandler(val client: Client) : InternalEventHandler<RoleCreateEvent> {

    override fun handle(data: JsonObject): RoleCreateEvent {
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")]!!
        val role = RoleData(guild, data.getValue("role").jsonObject)
        (guild as GuildData).roleCache[role.id] = role
        return RoleCreateEvent(role)
    }

}

class RoleUpdateEventHandler(val client: Client) : InternalEventHandler<RoleUpdateEvent> {

    override fun handle(data: JsonObject): RoleUpdateEvent {
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")]!!
        val role = RoleData(guild, data.getValue("role").jsonObject)
        (guild as GuildData).roleCache[role.id] = role
        return RoleUpdateEvent(role)
    }

}

class RoleDeleteEventHandler(val client: Client) : InternalEventHandler<RoleDeleteEvent> {

    override fun handle(data: JsonObject): RoleDeleteEvent {
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")]!!
        val roleId = data.getOrThrow<Snowflake>("role_id")
        (guild as GuildData).roleCache.remove(roleId)
        return RoleDeleteEvent(client, roleId)
    }

}