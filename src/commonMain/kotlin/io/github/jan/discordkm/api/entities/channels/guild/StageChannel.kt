/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.channels.guild

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PrivacyLevel
import io.github.jan.discordkm.api.entities.guild.cacheManager
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEventModifiable
import io.github.jan.discordkm.api.entities.guild.scheduled.event.ScheduledEventVoiceChannel
import io.github.jan.discordkm.api.entities.guild.stage.StageInstance
import io.github.jan.discordkm.api.entities.modifier.guild.GuildChannelBuilder
import io.github.jan.discordkm.api.entities.modifier.guild.VoiceChannelModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


sealed interface StageChannel : VoiceChannel {

    override val type: ChannelType
        get() = ChannelType.GUILD_STAGE_VOICE
    override val cache: StageChannelCacheEntry?
        get() = guild.cache?.cacheManager?.channelCache?.get(id) as? StageChannelCacheEntry

    /*
     * Creates a new stage instance in this [StageChannel]
     *
     * @param topic The topic of this stage instance
     * @param public Whether this stage instance will be available in Stage Discovery or not
     */
    suspend fun createInstance(topic: String, reason: String? = null) = client.buildRestAction<StageInstance> {
        route = Route.StageInstance.CREATE_INSTANCE.post(buildJsonObject {
            put("channel_id", id.long)
            put("topic", topic)
            put("privacy_level", PrivacyLevel.GUILD_ONLY.value)
        })
        transform { StageInstance(it.toJsonObject(), client) }
        this.reason = reason
    }

    /*
     * Retrieves the current stage instance, if this stage channel has one
     */
    suspend fun retrieveInstance() = client.buildRestAction<StageInstance> {
        route = Route.StageInstance.GET_INSTANCE(id).get()
        transform { StageInstance(it.toJsonObject(), client) }
    }

    companion object : GuildChannelBuilder<VoiceChannelModifier, StageChannel>, ScheduledEventModifiable<ScheduledEventVoiceChannel> {
        override fun createChannel(modifier: VoiceChannelModifier.() -> Unit) = VoiceChannelModifier(ChannelType.GUILD_STAGE_VOICE).apply(modifier)

        operator fun invoke(id: Snowflake, guild: Guild): StageChannel = StageChannelImpl(id, guild)
        operator fun invoke(data: JsonObject, guild: Guild) = ChannelSerializer.deserializeChannel<StageChannelCacheEntry>(data, guild)

        override fun createScheduledEvent(modifier: ScheduledEventVoiceChannel.() -> Unit) = ScheduledEventVoiceChannel(true).apply(modifier).build()
    }

}

internal class StageChannelImpl(override val id: Snowflake, override val guild: Guild) : StageChannel {

    override fun toString(): String = "StageChannel(id=$id, type=$type)"
    override fun equals(other: Any?): Boolean = other is StageChannel && other.id == id && other.guild.id == guild.id
    override fun hashCode(): Int = id.hashCode()

}

