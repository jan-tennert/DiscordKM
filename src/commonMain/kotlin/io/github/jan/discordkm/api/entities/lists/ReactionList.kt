package io.github.jan.discordkm.api.entities.lists

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.entities.UserData
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.put
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractClientEntity
import io.github.jan.discordkm.internal.utils.toJsonObject

class ReactionList(val message: Message) {


    suspend fun add(emoji: Emoji) = message.client.buildRestAction<Unit> {
        route = Route.Message.CREATE_REACTION(message.channel.id, message.id, emoji.asMention).put()
        transform {  }
    }


    suspend fun plusAssign(emoji: Emoji) = add(emoji)


    suspend fun deleteOwn(emoji: Emoji) = message.client.buildRestAction<Unit> {
        route = Route.Message.DELETE_OWN_REACTION(message.channel.id, message.id, emoji.asMention).delete()
        transform {  }
    }


    suspend fun delete(user: User, emoji: Emoji) = message.client.buildRestAction<Unit> {
        route = Route.Message.DELETE_USER_REACTION(message.channel.id, message.id, emoji.asMention, user.id).delete()
        transform {  }
    }


    suspend fun retrieveReactions(emoji: Emoji) = message.client.buildRestAction<List<User>> {
        route = Route.Message.GET_REACTIONS(message.channel.id, message.id, emoji.asMention).get()
        transform { it.toJsonObject().extractClientEntity(message.client) }
    }


    suspend fun clearReactions() = message.client.buildRestAction<Unit> {
        route = Route.Message.DELETE_REACTIONS(message.channel.id, message.id).delete()
        transform {  }
    }


    suspend fun clearReactions(emoji: Emoji) = message.client.buildRestAction<Unit> {
        route = Route.Message.DELETE_REACTION_EMOJI(message.channel.id, message.id, emoji.asMention).delete()
        transform {  }
    }

}