/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.GuildCacheEntry
import io.github.jan.discordkm.api.entities.guild.templates.GuildTemplate
import io.github.jan.discordkm.api.media.Image
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.GuildSerializer
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

open class GuildContainer(val client: DiscordClient) {

    /*
     * Retrieves a guild by its id.
     * Note: This guild **does not** include members, threads, channels, roles, etc.
     */
    suspend fun retrieve(id: Snowflake) = client.buildRestAction<GuildCacheEntry> {
        route = Route.Guild.GET_GUILD(id).get()
        transform { GuildSerializer.deserialize(it.toJsonObject(), client) }
    }

    /*
     * Creates a new guild
     */
    suspend fun create(templateCode: String, name: String, icon: Image? = null) = client.buildRestAction<Guild> {
        route = Route.Template.CREATE_GUILD_FROM_TEMPLATE(templateCode).post(buildJsonObject {
            put("name", name)
            put("icon", icon?.encodedData)
        })
        transform { GuildSerializer.deserialize(it.toJsonObject(), client) }
    }

    suspend fun create(template: GuildTemplate, name: String, icon: Image? = null) = create(template.code, name, icon)

}

class CacheGuildContainer(client: DiscordClient, override val values: Collection<GuildCacheEntry>) : NameableSnowflakeContainer<GuildCacheEntry>, GuildContainer(client)