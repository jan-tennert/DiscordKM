/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities

import io.github.jan.discordkm.api.entities.channels.PrivateChannel
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.caching.CacheEntity
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.FlagEnum
import io.github.jan.discordkm.internal.serialization.FlagSerializer
import io.github.jan.discordkm.internal.serialization.serializers.UserSerializer
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import io.github.jan.discordkm.internal.utils.snowflake
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.reflect.KProperty

sealed interface User : Mentionable, SnowflakeEntity, Reference<User>, BaseEntity, CacheEntity {

    override val asMention: String
        get() = "<@$id>"
    override val cache: UserCacheEntry?
        get() = client.users[id]
    /*
     * Creates a new [PrivateChannel]
     */
    suspend fun createPrivateChannel() = client.buildRestAction<PrivateChannel> {
        route = Route.User.CREATE_DM.post(buildJsonObject {
            put("recipient_id", id.long)
        })

        transform { PrivateChannel(it.toJsonObject()["id"]!!.snowflake, client) }
    }

    enum class PremiumType : EnumWithValue<Int> {
        NONE,
        NITRO_CLASSIC,
        NITRO;

        override val value: Int
            get() = ordinal

        companion object : EnumWithValueGetter<PremiumType, Int>(values())
    }

    enum class UserFlag(override val offset: Int) : FlagEnum<UserFlag> {
        DISCORD_EMPLOYEE(0),
        PARTNERED_SERVER_OWNER(1),
        HYPE_SQUAD_EVENTS(2),
        BUG_HUNTER_LEVEL_1(3),
        BUG_HUNTER_LEVEL_2(14),
        HOUSE_BRAVERY(6),
        HOUSE_BRILLIANCE(7),
        HOUSE_BALANCE(8),
        EARLY_SUPPORTER(9),
        TEAM_USER(10),
        VERIFIED_BOT(16),
        EARLY_VERIFIED_BOT_DEVELOPER(17),
        DISCORD_CERTIFIED_MODERATOR(18),
        BOT_HTTP_INTERACTIONS(19),
        UNKNOWN(-1);

        companion object : FlagSerializer<UserFlag>(values())
    }

    override fun getValue(ref: Any?, property: KProperty<*>) = client.users[id]!!

    override suspend fun retrieve() = client.users.retrieve(id)

    companion object {
        operator fun invoke(id: Snowflake, client: DiscordClient): User = UserImpl(id, client)
        operator fun invoke(data: JsonObject, client: DiscordClient) = UserSerializer.deserialize(data, client)
    }

}

internal class UserImpl(override val id: Snowflake, override val client: DiscordClient) : User {

    override fun toString() = "User(id=$id)"
    override fun hashCode() = id.hashCode()
    override fun equals(other: Any?) = other is User && other.id == id

}