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
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.media.Image
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.GuildSerializer
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

open class EmoteContainer(val guild: Guild) {

    /*
     * Retrieves a guild emote by its id
     */
    suspend fun retrieveEmote(id: Snowflake) = guild.client.buildRestAction<Emoji.Emote> {
        route = Route.Emoji.GET_GUILD_EMOJI(guild.id, id).get()
        transform { GuildSerializer.deserializeGuildEmote(it.toJsonObject(), guild.client) }
    }

    /*
     * Retrieves all guild emotes
     */
    suspend fun retrieveEmotes() = guild.client.buildRestAction<List<Emoji.Emote>> {
        route = Route.Emoji.GET_GUILD_EMOJIS(guild.id).get()
        transform { it.toJsonArray().map { e -> GuildSerializer.deserializeGuildEmote(e.jsonObject, guild.client) } }
    }

    /*
     * Creates a new guild emote
     * @param name The name of the emote
     * @param image The image of the emote itself
     * @param allowedRoleIds The roles that can use this emote
     */
    suspend fun create(name: String, image: Image, allowedRoleIds: List<Snowflake>) = guild.client.buildRestAction<Emoji.Emote> {
        route = Route.Emoji.CREATE_EMOJI(guild.id).post(buildJsonObject {
            put("name", name)
            put("image", image.encodedData)
            putJsonArray("roles") { allowedRoleIds.forEach { add(it.string) } }
        })
        transform { GuildSerializer.deserializeGuildEmote(it.toJsonObject(), guild.client) }
    }

    /*
     * Modifies an existing guild emote
     * @param name The name of the emote
     * @param allowedRoleIds The roles that can use this emote
     * @param reason The reason which will be displayed in the audit logs
     */
    suspend fun modify(id: Snowflake, name: String? = null, allowedRoleIds: List<Snowflake>? = null, reason: String? = null) = guild.client.buildRestAction<Emoji.Emote> {
        route = Route.Emoji.MODIFY_EMOJI(guild.id, id).post(buildJsonObject {
            putOptional("name", name)
            putJsonArray("roles") { allowedRoleIds?.forEach { add(it.string) } }
        })
        transform { GuildSerializer.deserializeGuildEmote(it.toJsonObject(), guild.client) }
        this.reason = reason
    }

    /*
     * Deletes a guild emote by its id
     * @param reason The reason which will be displayed in the audit logs
     */
    suspend fun delete(id: Snowflake, reason: String? = null) = guild.client.buildRestAction<Unit> {
        route = Route.Emoji.DELETE_EMOJI(guild.id, id).delete()
        this.reason = reason
    }

}

class CacheEmoteContainer(guild: Guild, override val values: Collection<Emoji.Emote>) : EmoteContainer(guild), NameableSnowflakeContainer<Emoji.Emote>