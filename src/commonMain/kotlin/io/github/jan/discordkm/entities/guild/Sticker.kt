package io.github.jan.discordkm.entities.guild

import io.github.jan.discordkm.Client
import io.github.jan.discordkm.entities.SerializableEntity
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.utils.getId
import io.github.jan.discordkm.utils.getOrDefault
import io.github.jan.discordkm.utils.getOrNull
import io.github.jan.discordkm.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlin.jvm.JvmName

class Sticker(override val data: JsonObject, override val client: Client) : Snowflake, SerializableEntity {

    override val id = data.getId()

    /**
     * If the sticker is a standard sticker, they come from a pack
     */
    val packId = data.getOrNull<Long>("pack_id")

    /**
     * The name of the sticker
     */
    val name = data.getOrThrow<String>("name")

    /**
     * The description of the sticker
     */
    val description = data.getOrNull<String>("description")

    /**
     * "For guild stickers, the Discord name of a unicode emoji representing the sticker's expression. for standard stickers, a comma-separated list of related expressions."
     */
    val tags = data.getValue("tags").jsonArray.map { it.jsonPrimitive.content }

    /**
     * [Type] of the sticker
     */
    val type = Type.values().first { it.ordinal + 1 == data.getOrThrow("type") }

    /**
     * [FormatType] of the sticker
     */
    val formatType = FormatType.values().first { it.ordinal + 1 == data.getOrThrow("format_type") }

    /**
     * If this sticker is available, can be null due to loss of server boosts
     */
    @get:JvmName("isAvailable")
    val isAvailable = data.getOrNull<Boolean>("available")

    /**
     * Returns a guild id if this sticker is from a guild
     */
    val guildId = data.getOrNull<Long>("guild_id")

    //user

    /**
     * The standard sticker's sort order within its pack
     */
    val sortValue = data.getOrDefault("sort_value", 0)

    override fun toString() = "Sticker[name=$name,id=$id,description=$description]"

    override fun equals(other: Any?): Boolean {
        if(other !is Sticker) return false
        return other.id == id
    }
    
    /**
     * Represents a [Sticker Item Object](https://discord.com/developers/docs/resources/sticker#sticker-item-object)
     */
    class Item(data: JsonObject) : Snowflake {

        val formatType = FormatType.values().first { it.ordinal + 1 == data.getValue("format_type").jsonPrimitive.int }

        val name = data.getOrThrow<String>("name")

        override val id = data.getId()

    }

    enum class FormatType {
        PNG,
        APNG,
        LOTTIE
    }

    enum class Type {
        STANDARD,
        GUILD
    }

}