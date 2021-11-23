/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild

import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.channels.guild.StageChannel
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.StageInstanceSerializer
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

interface StageInstance : SnowflakeEntity, BaseEntity {

    val stageChannel: StageChannel
    override val client: Client
        get() = stageChannel.client

    /**
     * Deletes this stage instance
     */
    suspend fun delete(reason: String? = null) = client.buildRestAction<Unit> {
        route = Route.StageInstance.DELETE_INSTANCE(stageChannel.id).delete()
        this.reason = reason
    }

    /**
     * Modifies this stage instance. All fields are optional
     * @param topic The new topic for this stage instance
     * @param privacyLevel The new privacy level for this stage instance
     */
    suspend fun modify(topic: String? = null, privacyLevel: PrivacyLevel? = null, reason: String? = null) = client.buildRestAction<StageInstanceCacheEntry> {
        route = Route.StageInstance.MODIFY_INSTANCE(stageChannel.id).patch(buildJsonObject {
            putOptional("topic", topic)
            putOptional("privacy_level", privacyLevel?.value)
        })
        transform { StageInstance(it.toJsonObject(), client) }
        this.reason = reason
    }

    companion object {
        operator fun invoke(id: Snowflake, channel: StageChannel) = object : StageInstance {
            override val id = id
            override val stageChannel = channel
        }
        operator fun invoke(data: JsonObject, client: Client) = StageInstanceSerializer.deserialize(data, client)
    }

}

class StageInstanceCacheEntry(
    override val guild: Guild,
    override val id: Snowflake,
    val topic: String,
    override val stageChannel: StageChannel,
    val privacyLevel: PrivacyLevel,
    val isDiscoveryEnabled: Boolean
) : StageInstance, GuildEntity {

    override val client: Client
        get() = stageChannel.client

}