package com.github.jan.discordkm.entities.guild

import com.github.jan.discordkm.entities.Mentionable
import com.github.jan.discordkm.entities.Reference
import com.github.jan.discordkm.entities.SerializableEntity
import com.github.jan.discordkm.entities.Snowflake
import com.github.jan.discordkm.utils.getColor
import com.github.jan.discordkm.utils.getEnums
import com.github.jan.discordkm.utils.getId
import com.github.jan.discordkm.utils.getOrThrow
import com.github.jan.discordkm.utils.getRoleTag
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

class Role(val guild: Guild, override val data: JsonObject) : Mentionable, Reference<Role>, Snowflake, SerializableEntity {

    override val id = data.getId()

    /**
     * Returns the name of the role
     */
    val name = data.getOrThrow<String>("name")

    /**
     * Returns the color of the role
     */
    val color = data.getColor("color")

    /**
     * If this role is pinned in the user listing
     */
    @get:JvmName("isHoist")
    val isHoist = data.getOrThrow<Boolean>("hoist")

    /**
     * The position of this role
     */
    val position = data.getOrThrow<Int>("position")

    /**
     * The permissions which this role has
     */
    val permissions = data.getEnums("permissions", Permission)

    /**
     * Whether this role is managed by an interaction
     */
    @get:JvmName("isManaged")
    val isManaged = data.getOrThrow<Boolean>("managed")

    /**
     * Whether this role is mentionable
     */
    @get:JvmName("isMentionable")
    val isMentionable = data.getOrThrow<Boolean>("mentionable")

    /**
     * The tags this role has
     */
    val tags = data.getRoleTag("tags")
    override val client = guild.client

    override val asMention = "<@&$id>"

    override fun getValue(ref: Any?, property: KProperty<*>) = guild.roles[id]!!

    override fun toString() = "Role[name=$name, id=$id]"

    override fun equals(other: Any?): Boolean {
        if(other !is Role) return false
        return other.id == id
    }

    class Tag(val botId: Long? = null, integrationId: Long? = null, val premiumSubscriber: Boolean? = null)
}