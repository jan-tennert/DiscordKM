/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.channels

import io.github.jan.discordkm.Cache
import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.User
import io.github.jan.discordkm.entities.messages.Message
import io.github.jan.discordkm.utils.extractClientEntity
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class PrivateChannel(override val client: Client, override val data: JsonObject) : MessageChannel {

    override val messageCache: Cache<Message> = Cache.fromSnowflakeEntityList(emptyList())

    override val type = ChannelType.DM

    val recipients = data.getValue("recipients").jsonArray.map { it.jsonObject.extractClientEntity<User>(client) }

}