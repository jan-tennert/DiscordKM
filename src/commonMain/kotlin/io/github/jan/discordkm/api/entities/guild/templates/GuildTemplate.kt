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
import com.soywiz.klock.ISO8601
import com.soywiz.klock.parse
import io.github.jan.discordkm.api.entities.SerializableEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.internal.utils.extractClientEntity
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.jsonObject

/**
 * A guild template is used to create a guild on top of another guild. Roles, channels, etc. are synced
 */
interface GuildTemplate : SerializableEntity {

    /**
     * The template code
     */
    val code: String
        get() = data.getOrThrow("code")

    /**
     * The name of this template
     */
    val name: String
        get() = data.getOrThrow("name")

    /**
     * The description of this template
     */
    val description: String?
        get() = data.getOrNull("description")

    /**
     * Number of times this template has been used
     */
    val usageCount: Int
        get() = data.getOrThrow("usage_count")

    /**
     * The creator id of this template
     */
    val creatorId: Snowflake
        get() = data.getOrThrow("creator_id")

    /**
     * The creator of this template
     */
    val creator: User
        get() = data.getValue("creator").jsonObject.extractClientEntity(client)

    /**
     * When this template was created at
     */
    val createdAt: DateTimeTz
        get() = ISO8601.DATETIME_UTC_COMPLETE.parse(data.getOrThrow<String>("created_at"))

    /**
     * When this template was last synced to the source guild
     */
    val uploadedAt: DateTimeTz
        get() = ISO8601.DATETIME_UTC_COMPLETE.parse(data.getOrThrow<String>("updated_at"))

    /**
     * The guild this template is based on
     */
    val sourceGuildId: Snowflake
        get() = data.getOrThrow("source_guild_id")

    /**
     * The guild this template is based on
     */
    val sourceGuild: Guild
        get() = data.getValue("serialized_source_guild").jsonObject.extractClientEntity(client)

    /**
     * Whether this template has unsynced changed
     */
    val isDirty: Boolean
        get() = data.getOrNull<Boolean>("is_dirty") ?: false

    val url: String
        get() = "https://discord.new/$code"

    /**
     * Deletes this guild template
     *
     * Requires the [MANAGE_GUILD] Permission
     */
    suspend fun delete()

    /**
    * Modifies this guild template
     *
    * Requires the [MANAGE_GUILD] Permission
    */
    suspend fun modify(name: String? = null, description: String? = null) : GuildTemplate

    /**
     * Syncs the template to the guild's current state
     */
    suspend fun sync(): GuildTemplate

}