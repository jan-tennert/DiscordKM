package io.github.jan.discordkm.entities.lists

import io.github.jan.discordkm.entities.User
import io.github.jan.discordkm.entities.guild.Emoji
import io.github.jan.discordkm.entities.messages.Message
import io.github.jan.discordkm.restaction.CallsTheAPI
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractClientEntity
import io.github.jan.discordkm.utils.toJsonObject

class ReactionList(val message: Message) {

    @CallsTheAPI
    suspend fun add(emoji: Emoji) = message.client.buildRestAction<Unit> {
        action = RestAction.Action.put("/channels/${message.channel.id}/messages/${message.id}/reactions/${emoji.name}/@me")
        transform {  }
    }

    @CallsTheAPI
    suspend fun plusAssign(emoji: Emoji) = add(emoji)

    @CallsTheAPI
    suspend fun deleteOwn(emoji: Emoji) = message.client.buildRestAction<Unit> {
        action = RestAction.Action.delete("/channels/${message.channel.id}/messages/${message.id}/reactions/${emoji.name}/@me")
        transform {  }
    }

    @CallsTheAPI
    suspend fun delete(user: User, emoji: Emoji) = message.client.buildRestAction<Unit> {
        action = RestAction.Action.delete("/channels/${message.channel.id}/messages/${message.id}/reactions/${emoji.name}/${user.id}")
        transform {  }
    }

    @CallsTheAPI
    suspend fun retrieveReactions(emoji: Emoji) = message.client.buildRestAction<List<User>> {
        action = RestAction.Action.get("/channels/${message.channel.id}/messages/${message.id}/reactions/${emoji.name}")
        transform { it.toJsonObject().extractClientEntity(message.client) }
    }

    @CallsTheAPI
    suspend fun clearReactions() = message.client.buildRestAction<Unit> {
        action = RestAction.Action.delete("/channels/${message.channel.id}/messages/${message.id}/reactions")
        transform {  }
    }

    @CallsTheAPI
    suspend fun clearReactions(emoji: Emoji) = message.client.buildRestAction<Unit> {
        action = RestAction.Action.delete("/channels/${message.channel.id}/messages/${message.id}/reactions/${emoji.name}")
    }

}