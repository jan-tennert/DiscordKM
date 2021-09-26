/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild.channels

import io.github.jan.discordkm.api.entities.guild.StageInstance
import io.github.jan.discordkm.api.entities.guild.channels.modifier.GuildChannelBuilder
import io.github.jan.discordkm.api.entities.guild.channels.modifier.VoiceChannelModifier
import io.github.jan.discordkm.api.entities.lists.retrieve
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface StageChannel : VoiceChannel {


    override suspend fun modify(modifier: VoiceChannelModifier.() -> Unit): StageChannel

    override suspend fun retrieve() = guild.channels.retrieve(id) as StageChannel

    /**
     * Creates a new stage instance in this [StageChannel]
     */
    suspend fun createInstance(topic: String, privacyLevel: StageInstance.PrivacyLevel = StageInstance.PrivacyLevel.GUILD_ONLY) = client.buildRestAction<StageInstance> {
        route = Route.StageInstance.CREATE_INSTANCE.post(buildJsonObject {
            put("channel_id", id.long)
            put("topic", topic)
            put("privacy_level", privacyLevel.ordinal)
        })
        transform { StageInstance(guild, it.toJsonObject()) }
        onFinish {  }
    }

    suspend fun retrieveInstance() = client.buildRestAction<StageInstance> {
        route = Route.StageInstance.GET_INSTANCE(id).get()
        transform { StageInstance(guild, it.toJsonObject()) }
    }

    companion object : GuildChannelBuilder<VoiceChannel, VoiceChannelModifier> {

        override fun create(builder: VoiceChannelModifier.() -> Unit) = VoiceChannelModifier(13).apply(builder).build()

    }

}