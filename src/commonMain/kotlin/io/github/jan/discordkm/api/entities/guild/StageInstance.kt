/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild

import io.github.jan.discordkm.api.entities.Reference
import io.github.jan.discordkm.api.entities.SerializableEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.channels.StageChannel
import io.github.jan.discordkm.api.entities.lists.getGuildChannel
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.getId
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonObject
import io.github.jan.discordkm.internal.utils.valueOfIndex
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

class StageInstance(override val client: Client, override val data: JsonObject) : SerializableEntity, Reference<StageInstance>, SnowflakeEntity {

    override val id = data.getId()
    val guildId = data.getOrThrow<Snowflake>("guild_id")
    val guild: Guild
        get() = client.guilds[guildId]!!

    /**
     * The stage channel where the stage instance is in
     */
    val stageChannelId = data.getOrThrow<Snowflake>("channel_id")

    /**
     * The stage channel where the stage instance is in
     */
    val stageChannel
        get() = guild.channels.getGuildChannel<StageChannel>(stageChannelId)

    /**
     * The topic of the stage instance
     */
    val topic = data.getOrThrow<String>("topic")

    /**
     * The [PrivacyLevel] of the stage instance
     */
    val privacyLevel = valueOfIndex<PrivacyLevel>(data.getOrThrow("privacy_level"), 1)

    /**
     * Whether stage discovery is enabled
     */
    @get:JvmName("isDiscovery")
    val isDiscovery = data.getOrThrow<Boolean>("discoverable_disabled")

    override fun getValue(ref: Any?, property: KProperty<*>) = guild.stageInstances.first { it.id == id }

    /**
     * Deletes this stage instance
     */
    suspend fun delete() = client.buildRestAction<Unit> {
        route = Route.StageInstance.DELETE_INSTANCE(stageChannelId).delete()
        transform {  }
        onFinish { (guild as GuildData).stageInstanceCache.remove(id) }
    }

    /**
     * Modifies this stage instance. All fields are optional
     * @param topic The new topic for this stage instance
     * @param privacyLevel The new privacy level for this stage instance
     */
    suspend fun modify(topic: String? = null, privacyLevel: PrivacyLevel? = null) = client.buildRestAction<StageInstance> {
        route = Route.StageInstance.MODIFY_INSTANCE(stageChannelId).patch(buildJsonObject {
            putOptional("topic", topic)
            putOptional("privacy_level", privacyLevel?.ordinal)
        })
        transform { StageInstance(client, it.toJsonObject()) }
        onFinish { (guild as GuildData).stageInstanceCache[id] = it }
    }

    override suspend fun retrieve() = stageChannel.retrieveInstance()

    enum class PrivacyLevel {
        PUBLIC,
        GUILD_ONLY
    }

}