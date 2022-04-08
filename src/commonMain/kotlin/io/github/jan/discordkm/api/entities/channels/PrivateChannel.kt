/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.channels

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.DiscordClient

sealed interface PrivateChannel : MessageChannel {

    override val type: ChannelType
        get() = ChannelType.DM

    companion object {
        operator fun invoke(id: Snowflake, client: DiscordClient): PrivateChannel = PrivateChannelImpl(id, client)
    }

}

internal class PrivateChannelImpl(override val id: Snowflake, override val client: DiscordClient) : PrivateChannel {
    override val cache: Nothing? = null

    override fun toString(): String = "PrivateChannel(id=$id)"
    override fun hashCode(): Int = id.hashCode()
    override fun equals(other: Any?): Boolean = other is PrivateChannel && other.id == id
}