/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.entities.guilds.templates

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.templates.GuildTemplate
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.put
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class GuildTemplateData(override val client: Client, override val data: JsonObject) : GuildTemplate {

    override suspend fun delete() = client.buildRestAction<Unit> {
        route = Route.Template.DELETE_GUILD_TEMPLATE(sourceGuildId, code).delete()
        transform {  }
    }

    override suspend fun modify(name: String?, description: String?) = client.buildRestAction<GuildTemplate> {
        route = Route.Template.MODIFY_GUILD_TEMPLATE(sourceGuildId, code).patch(buildJsonObject {
            put("name", name)
            putOptional("description", description)
        })
        transform { GuildTemplateData(client, it.toJsonObject()) }
    }

    override suspend fun sync() = client.buildRestAction<GuildTemplate> {
        route = Route.Template.SYNC_GUILD_TEMPLATE(sourceGuildId, code).put()
        transform { GuildTemplateData(client, it.toJsonObject()) }
    }

}