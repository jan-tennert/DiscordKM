/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.UserCacheEntry
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.clients.WSDiscordClientImpl
import io.github.jan.discordkm.api.events.SelfUserUpdateEvent
import kotlinx.serialization.json.JsonObject

internal class SelfUserUpdateEventHandler(val client: DiscordClient) : InternalEventHandler<SelfUserUpdateEvent> {

    override suspend fun handle(data: JsonObject): SelfUserUpdateEvent {
        val user = User(data, client)
        val oldUser = client.selfUser
        (client as WSDiscordClientImpl).updateSelfUser(user)
        return SelfUserUpdateEvent(user, oldUser as UserCacheEntry)
    }

}