/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.UserCacheEntry
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.api.entities.messages.MessageReaction
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.put
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.toJsonArray
import kotlinx.serialization.json.jsonObject

open class ReactionContainer(val message: Message) {

    /*
     * Adds a new reaction to the message
     */
    suspend fun add(emoji: Emoji) = message.client.buildRestAction<Unit> {
        route = Route.Message.CREATE_REACTION(message.channel.id, message.id, emoji.asMention).put()
    }

    suspend fun plusAssign(emoji: Emoji) = add(emoji)

    /*
     * Deletes your own reaction with the specified emoji
     */
    suspend fun deleteOwn(emoji: Emoji) = message.client.buildRestAction<Unit> {
        route = Route.Message.DELETE_OWN_REACTION(message.channel.id, message.id, emoji.asMention).delete()
    }

    /*
     * Deletes a user's reaction
      */
    suspend fun delete(user: User, emoji: Emoji) = message.client.buildRestAction<Unit> {
        route = Route.Message.DELETE_USER_REACTION(message.channel.id, message.id, emoji.asMention, user.id).delete()
    }

    /*
     * Retrieves all reactions with the specified emoji
     */
    suspend fun retrieveReactions(emoji: Emoji) = message.client.buildRestAction<List<UserCacheEntry>> {
        route = Route.Message.GET_REACTIONS(message.channel.id, message.id, emoji.asMention).get()
        transform { it.toJsonArray().map{ json -> User(json.jsonObject, message.client)} }
    }

    /*
     * Removes all reactions
     */
    suspend fun clearReactions() = message.client.buildRestAction<Unit> {
        route = Route.Message.DELETE_REACTIONS(message.channel.id, message.id).delete()
    }

    /*
     * Removes all reactions with the specified emoji
     */
    suspend fun clearReactions(emoji: Emoji) = message.client.buildRestAction<Unit> {
        route = Route.Message.DELETE_REACTION_EMOJI(message.channel.id, message.id, emoji.asMention).delete()
    }

}

class CacheReactionContainer(message: Message, val values: List<MessageReaction>) : ReactionContainer(message), Iterable<MessageReaction> {

    override fun iterator() = values.iterator()

}