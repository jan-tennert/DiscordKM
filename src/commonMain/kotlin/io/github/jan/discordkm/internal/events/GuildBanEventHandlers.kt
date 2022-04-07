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
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.events.BanEvent
import io.github.jan.discordkm.api.events.GuildBanAddEvent
import io.github.jan.discordkm.api.events.GuildBanRemoveEvent
import io.github.jan.discordkm.internal.utils.snowflake
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

internal class BanEventHandler(val client: DiscordClient) {

    inline fun <reified C : BanEvent> handle(data: JsonObject): C {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val user = User(data["user"]!!.jsonObject, client)
        return when(C::class) {
            GuildBanAddEvent::class -> GuildBanAddEvent(guild, user) as C
            GuildBanRemoveEvent::class -> GuildBanRemoveEvent(guild, user) as C
            else -> throw IllegalStateException()
        }
    }

}