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