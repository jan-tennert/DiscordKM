/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.serialization.serializers

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.UserCacheEntry
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.internal.utils.boolean
import io.github.jan.discordkm.internal.utils.get
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.long
import kotlinx.serialization.json.JsonObject


object UserSerializer {

    fun deserialize(data: JsonObject, value: Client) = UserCacheEntry(
        id = data.getOrThrow("id"),
        name = data["username"].toString(),
        discriminator = data["discriminator"].toString(),
        avatarHash = data["avatar"]?.toString(),
        isBot = data["bot", true]?.boolean ?: false,
        hasMfaEnabled = data["mfa_enabled", true]?.boolean ?: false,
        flags = data["flags", true]?.long?.let { User.UserFlag.decode(it) } ?: setOf(),
        premiumType = data["premium_type", true]?.int?.let { User.PremiumType.get(it) } ?: User.PremiumType.NONE,
        publicFlags = data["flags", true]?.long?.let { User.UserFlag.decode(it) } ?: setOf(),
        isSystem = data["system", true]?.boolean ?: false,
        client = value
    )

}