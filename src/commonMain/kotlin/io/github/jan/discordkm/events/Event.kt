/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.events

import io.github.jan.discordkm.entities.BaseEntity
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.channels.MessageChannel
import io.github.jan.discordkm.entities.interactions.Interaction
import io.github.jan.discordkm.entities.messages.Message
import io.github.jan.discordkm.restaction.CallsTheAPI
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.toJsonObject

sealed interface Event : BaseEntity

sealed interface MessageEvent : Event {

    val messageId: Snowflake

    val channelId: Snowflake

    val channel: MessageChannel

    @CallsTheAPI
    suspend fun retrieveMessage() = client.buildRestAction<Message> {
        action = RestAction.Action.get("/channels/${channelId}/messages/$messageId")
        transform { Message(channel, it.toJsonObject()) }
    }

}

sealed interface InteractionCreateEvent : Event {

    val interaction: Interaction

}

sealed interface GuildEvent : Event {

    val guildId: Snowflake

    @CallsTheAPI
    suspend fun retrieveGuild() = client.guilds.retrieve(guildId)

}
