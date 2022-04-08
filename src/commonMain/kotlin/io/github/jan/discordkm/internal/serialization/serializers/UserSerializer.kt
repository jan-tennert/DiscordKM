/*
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
import io.github.jan.discordkm.api.entities.UserCacheEntryImpl
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.misc.Color
import io.github.jan.discordkm.internal.utils.boolean
import io.github.jan.discordkm.internal.utils.get
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.long
import io.github.jan.discordkm.internal.utils.snowflake
import io.github.jan.discordkm.internal.utils.string
import kotlinx.serialization.json.JsonObject


internal object UserSerializer {

    fun deserialize(data: JsonObject, value: DiscordClient): UserCacheEntry = UserCacheEntryImpl(
        id = data["id"]!!.snowflake,
        name = data["username", true]?.string ?: "",
        discriminator = data["discriminator", true]?.string ?: "",
        avatarHash = data["avatar", true]?.string,
        isBot = data["bot", true]?.boolean ?: false,
        hasMfaEnabled = data["mfa_enabled", true]?.boolean ?: false,
        flags = data["flags", true]?.long?.let { User.UserFlag.decode(it) } ?: setOf(),
        premiumType = data["premium_type", true]?.int?.let { User.PremiumType.get(it) } ?: User.PremiumType.NONE,
        publicFlags = data["flags", true]?.long?.let { User.UserFlag.decode(it) } ?: setOf(),
        isSystem = data["system", true]?.boolean ?: false,
        accentColor = data["accent_color", true]?.string?.let { Color.fromHex(it) },
        bannerHash = data["banner", true]?.string,
        client = value
    )

}