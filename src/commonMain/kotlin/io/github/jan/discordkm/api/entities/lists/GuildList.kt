/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.lists

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.templates.GuildTemplate
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.api.media.Image
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractClientEntity
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class GuildList(val client: Client, override val internalList: List<Guild>) : DiscordList<Guild> {

    override fun get(name: String) = internalList.filter { it.name == name }
    
    suspend fun retrieve(id: Snowflake) = client.buildRestAction<Guild> {
        route = Route.Guild.GET_GUILD(id).get()
        transform { it.toJsonObject().extractClientEntity(client) }
    }

    suspend fun create(templateCode: String, name: String, icon: Image? = null) = client.buildRestAction<Guild> {
        route = Route.Template.CREATE_GUILD_FROM_TEMPLATE(templateCode).post(buildJsonObject {
            put("name", name)
            put("icon", icon?.encodedData)
        })
        transform { GuildData(client, it.toJsonObject()) }
    }

    suspend fun create(template: GuildTemplate, name: String, icon: Image? = null) = create(template.code, name, icon)

}

