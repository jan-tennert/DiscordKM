package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.events.GuildMemberAddEvent
import io.github.jan.discordkm.api.events.GuildMemberRemoveEvent
import io.github.jan.discordkm.api.events.GuildMemberUpdateEvent
import io.github.jan.discordkm.internal.entities.UserData
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.entities.guilds.MemberData
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class GuildMemberAddEventHandler(val client: Client) : InternalEventHandler<GuildMemberAddEvent> {

    override fun handle(data: JsonObject): GuildMemberAddEvent {
        val guildId = data.getOrThrow<Snowflake>("guild_id")
        val guild = client.guilds[guildId]!!
        val member = MemberData(guild, data)
        (guild as GuildData).memberCache[member.user.id] = member
        return GuildMemberAddEvent(member)
    }

}

class GuildMemberUpdateEventHandler(val client: Client) : InternalEventHandler<GuildMemberUpdateEvent> {

    override fun handle(data: JsonObject): GuildMemberUpdateEvent {
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")]!!
        val member = MemberData(guild, data)
        (guild as GuildData).memberCache[member.user.id] = member
        return GuildMemberUpdateEvent(member)
    }

}

class GuildMemberRemoveEventHandler(val client: Client) : InternalEventHandler<GuildMemberRemoveEvent> {

    override fun handle(data: JsonObject): GuildMemberRemoveEvent {
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")]!!
        val user = UserData(client, data.getValue("user").jsonObject)
        (guild as GuildData).memberCache.remove(user.id)
        return GuildMemberRemoveEvent(guild, user)
    }

}