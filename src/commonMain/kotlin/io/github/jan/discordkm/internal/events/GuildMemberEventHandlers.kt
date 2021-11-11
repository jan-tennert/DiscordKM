/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.events.GuildMemberAddEvent
import io.github.jan.discordkm.api.events.GuildMemberRemoveEvent
import io.github.jan.discordkm.api.events.GuildMemberUpdateEvent
import io.github.jan.discordkm.internal.caching.Cache
import io.github.jan.discordkm.internal.entities.UserData
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.entities.guilds.MemberData
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class GuildMemberAddEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<GuildMemberAddEvent> {

    override fun handle(data: JsonObject): GuildMemberAddEvent {
        val guildId = data.getOrThrow<Snowflake>("guild_id")
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")] ?: throw IllegalStateException("Guild with id $guildId couldn't be found on an event. The guilds probably aren't done initialising.")
        val member = MemberData(guild, data)
        if(Cache.MEMBERS in client.enabledCache) (guild as GuildData).memberCache[member.user.id] = member
        return GuildMemberAddEvent(member)
    }

}

class GuildMemberUpdateEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<GuildMemberUpdateEvent> {

    override fun handle(data: JsonObject): GuildMemberUpdateEvent {
        val guildId = data.getOrThrow<Snowflake>("guild_id")
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")] ?: throw IllegalStateException("Guild with id $guildId couldn't be found on an event. The guilds probably aren't done initialising.")
        val member = MemberData(guild, data)
        if(Cache.MEMBERS in client.enabledCache) (guild as GuildData).memberCache[member.user.id] = member
        return GuildMemberUpdateEvent(member)
    }

}

class GuildMemberRemoveEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<GuildMemberRemoveEvent> {

    override fun handle(data: JsonObject): GuildMemberRemoveEvent {
        val guildId = data.getOrThrow<Snowflake>("guild_id")
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")] ?: throw IllegalStateException("Guild with id $guildId couldn't be found on an event. The guilds probably aren't done initialising.")
        val user = UserData(client, data.getValue("user").jsonObject)
        if(Cache.MEMBERS in client.enabledCache) (guild as GuildData).memberCache.remove(user.id)
        return GuildMemberRemoveEvent(guild, user)
    }

}