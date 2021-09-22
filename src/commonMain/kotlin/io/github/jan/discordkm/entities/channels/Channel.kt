/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.channels

import io.github.jan.discordkm.entities.Mentionable
import io.github.jan.discordkm.entities.SerializableEntity
import io.github.jan.discordkm.entities.SnowflakeEntity
import io.github.jan.discordkm.entities.interactions.commands.ChannelTypeSerializer
import io.github.jan.discordkm.restaction.CallsTheAPI
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.getId
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

interface Channel : Mentionable, SnowflakeEntity, SerializableEntity {

    val type: ChannelType
        get() = ChannelType.values().first { it.id == data.getValue("type").jsonPrimitive.int }

    @CallsTheAPI
    suspend fun delete() = client.buildRestAction<Unit> {
        action = RestAction.Action.delete("/channels/$id")
        transform {  }
        check {
            //TODO: check for permission
        }
    }

    override val id
        get() = data.getId()

    override val asMention
        get() = "<#$id>"

}

@Serializable(with = ChannelTypeSerializer::class)
enum class ChannelType(val id: Int) {
    GUILD_TEXT(0),
    DM(1),
    GUILD_VOICE(2),
    GROUP_DM(3),
    GUILD_CATEGORY(4),
    GUILD_NEWS(5),
    GUILD_STORE(6),
    GUILD_NEWS_THREAD(10),
    GUILD_PUBLIC_THREAD(11),
    GUILD_PRIVATE_THREAD(12),
    GUILD_STAGE_VOICE(13)
}