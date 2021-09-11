package io.github.jan.discordkm.entities.guild

import com.soywiz.klock.ISO8601
import com.soywiz.klock.parse
import io.github.jan.discordkm.entities.Reference
import io.github.jan.discordkm.entities.SerializableEntity
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.User
import io.github.jan.discordkm.entities.misc.RoleList
import io.github.jan.discordkm.utils.extractClientEntity
import io.github.jan.discordkm.utils.getOrDefault
import io.github.jan.discordkm.utils.getOrNull
import io.github.jan.discordkm.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

class Member(val guild: Guild, override val data: JsonObject) : Reference<Member>, Snowflake, SerializableEntity {

    override val client = guild.client

    /**
     * Returns the [User] of this member
     */
    var user = data.getValue("user").jsonObject.extractClientEntity<User>(guild.client)
        private set

    /**
     * Returns the nickname of the member. If the member doesn't have a nickname it returns his real name
     */
    val nickname = data.getOrDefault("nick", user.name)

    /**
     * Returns the roles of the member
     */
    val roles = RoleList(guild, data.getValue("roles").jsonArray.map { guild.roles[it.jsonPrimitive.long]!! })

    /**
     * Returns the date when the member joined his guild
     */
    val joinedAt = ISO8601.DATETIME_UTC_COMPLETE.parse(data.getOrThrow("joined_at"))

    /**
     * Returns the date when the member boosted his guild. Can be null if the user isn't boosting his server
     */
    val premiumSince = if(data.getOrNull<String>("premium_since") != null) ISO8601.DATETIME_UTC_COMPLETE.parse(data.getOrThrow("premium_since")) else null

    /**
     * Whether the member is deafened
     */
    @get:JvmName("isDeafened")
    val isDeafened = data.getOrThrow<Boolean>("deaf")

    /**
     * Whether the member is muted
     */
    @get:JvmName("isMuted")
    val isMuted = data.getOrThrow<Boolean>("mute")

    /**
     * Whether the member hasn't passed the guilds Membership screen requirements
     */
    val isPending = data.getOrDefault("pending", false)

    override fun getValue(ref: Any?, property: KProperty<*>) = guild.members[id]!!

    override fun toString() = "Member[nickname=$nickname, id=$id]"

    override fun equals(other: Any?): Boolean {
        if(other !is Member) return false
        return other.id == id
    }
    
    override val id = user.id

}