package io.github.jan.discordkm.api.entities.lists

import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.internal.entities.UserData
import io.github.jan.discordkm.internal.restaction.RestAction
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractClientEntity
import io.github.jan.discordkm.internal.utils.toJsonObject

class ReactionList(val message: Message) {


    suspend fun add(emoji: Emoji) = message.client.buildRestAction<Unit> {
        action = RestAction.put("/channels/${message.channel.id}/messages/${message.id}/reactions/${emoji.name}/@me")
        transform {  }
    }


    suspend fun plusAssign(emoji: Emoji) = add(emoji)


    suspend fun deleteOwn(emoji: Emoji) = message.client.buildRestAction<Unit> {
        action = RestAction.delete("/channels/${message.channel.id}/messages/${message.id}/reactions/${emoji.name}/@me")
        transform {  }
    }


    suspend fun delete(user: UserData, emoji: Emoji) = message.client.buildRestAction<Unit> {
        action = RestAction.delete("/channels/${message.channel.id}/messages/${message.id}/reactions/${emoji.name}/${user.id}")
        transform {  }
    }


    suspend fun retrieveReactions(emoji: Emoji) = message.client.buildRestAction<List<UserData>> {
        action = RestAction.get("/channels/${message.channel.id}/messages/${message.id}/reactions/${emoji.name}")
        transform { it.toJsonObject().extractClientEntity(message.client) }
    }


    suspend fun clearReactions() = message.client.buildRestAction<Unit> {
        action = RestAction.delete("/channels/${message.channel.id}/messages/${message.id}/reactions")
        transform {  }
    }


    suspend fun clearReactions(emoji: Emoji) = message.client.buildRestAction<Unit> {
        action = RestAction.delete("/channels/${message.channel.id}/messages/${message.id}/reactions/${emoji.name}")
    }

}