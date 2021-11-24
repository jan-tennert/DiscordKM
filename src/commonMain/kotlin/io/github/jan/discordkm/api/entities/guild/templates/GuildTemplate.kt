/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild.templates

import com.soywiz.klock.DateTimeTz
import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.put
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.GuildSerializer
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Represents a guild template
 * @param code The template code
 * @param name The name of this template
 * @param description The description of this template
 * @param usageCount Number of times this template has been used
 * @param creator The creator of this template
 * @param createdAt When this template was created at
 * @param updatedAt When this template was last synced to the source guild
 * @param sourceGuild The guild this template is based on
 * @param isDirty Whether this template has unsynced changes
 */
data class GuildTemplate(
    val code: String,
    val name: String,
    val description: String?,
    val usageCount: Int,
    val creator: User,
    val createdAt: DateTimeTz,
    val updatedAt: DateTimeTz,
    val sourceGuild: Guild,
    val isDirty: Boolean,
    override val client: Client
) : BaseEntity {


    /**
     * Deletes this guild template
     */
    suspend fun delete() = client.buildRestAction<Unit> {
        route = Route.Template.DELETE_GUILD_TEMPLATE(sourceGuild.id, code).delete()
    }

    /**
     * Modifies this guild template
     * @param name The new name of the template
     * @param description The new description of the template
     */
    suspend fun modify(name: String?, description: String?) = client.buildRestAction<GuildTemplate> {
        route = Route.Template.MODIFY_GUILD_TEMPLATE(sourceGuild.id, code).patch(buildJsonObject {
            put("name", name)
            putOptional("description", description)
        })
        transform { copy(name = name ?: this@GuildTemplate.name, description = description ?: this@GuildTemplate.name) }
    }

    /**
     * Syncs the template to the source guild's current state
     */
    suspend fun sync() = client.buildRestAction<GuildTemplate> {
        route = Route.Template.SYNC_GUILD_TEMPLATE(sourceGuild.id, code).put()
        transform { GuildSerializer.deserializeGuildTemplate(it.toJsonObject(), client) }
    }

}

