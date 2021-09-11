package com.github.jan.discordkm.entities.channels

import com.github.jan.discordkm.entities.Mentionable
import com.github.jan.discordkm.entities.SerializableEntity
import com.github.jan.discordkm.entities.Snowflake
import com.github.jan.discordkm.restaction.RestAction
import com.github.jan.discordkm.restaction.buildRestAction
import com.github.jan.discordkm.utils.getId
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

interface Channel : Mentionable, Snowflake, SerializableEntity {

    val type
        get() = Type.values().first { it.ordinal == data.getValue("type").jsonPrimitive.int }

    suspend fun delete() = client.buildRestAction<Unit> {
        action = RestAction.Action.delete("/channels/$id")
        transform {  }
    }

    override val id
        get() = data.getId()

    override val asMention
        get() = "<#$id>"

    enum class Type(val id: Int) {
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

}