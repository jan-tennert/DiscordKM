package com.github.jan.discordkm.entities.guild

import com.github.jan.discordkm.Client
import com.github.jan.discordkm.entities.SerializableEntity
import com.github.jan.discordkm.entities.Snowflake
import com.github.jan.discordkm.utils.getId
import com.github.jan.discordkm.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmName

class Emoji(override val data: JsonObject, override val client: Client) : Snowflake, SerializableEntity {

    override val id = data.getId()
    val name = data.getOrThrow<String>("name")

    @get:JvmName("isAnimated")
    val isAnimated = data.getOrThrow<Boolean>("animated")
    //allowed users?
    //managed?
    //require colons?

    /**
     * If the emoji is available, can be null due to loss of server boosts
     */
    @get:JvmName("isAvailable")
    val isAvailable = data.getOrThrow<Boolean>("available")

    override fun equals(other: Any?): Boolean {
        if(other !is Emoji) return false
        return other.name == name && other.id == id
    }

}