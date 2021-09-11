package io.github.jan.discordkm.entities.guild

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.SerializableEntity
import io.github.jan.discordkm.entities.SnowflakeEntity
import io.github.jan.discordkm.utils.getId
import io.github.jan.discordkm.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmName

class Emoji(override val data: JsonObject, override val client: Client) : SnowflakeEntity, SerializableEntity {

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