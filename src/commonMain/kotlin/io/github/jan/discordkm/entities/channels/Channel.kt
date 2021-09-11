package io.github.jan.discordkm.entities.channels

import io.github.jan.discordkm.entities.Mentionable
import io.github.jan.discordkm.entities.SerializableEntity
import io.github.jan.discordkm.entities.SnowflakeEntity
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.getId
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

interface Channel : Mentionable, SnowflakeEntity, SerializableEntity {

    val type: ChannelType
        get() = ChannelType.values().first { it.id == data.getValue("type").jsonPrimitive.int }

    suspend fun delete() = client.buildRestAction<Unit> {
        action = RestAction.Action.delete("/channels/$id")
        transform {  }
        check {
            //TODO: check for permission
        }
    }

    override val id
        get() = data.getId()

    override val asMention
        get() = "<#$id>"

}

enum class ChannelType(val id: Int) {
    GUILD_TEXT(0),
    DM(1),
    GUILD_VOICE(2),
    GROUP_DM(3),
    GUILD_CATEGORY(4),
    GUILD_NEWS(5),
    GUILD_STORE(6),
    GUILD_NEWS_THREAD(10),
    GUILD_PUBLIC_THREAD(11),
    GUILD_PRIVATE_THREAD(12),
    GUILD_STAGE_VOICE(13)
}