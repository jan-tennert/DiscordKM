package io.github.jan.discordkm.internal.entities.guilds

import io.github.jan.discordkm.internal.Cache
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.api.entities.guild.VoiceState
import io.github.jan.discordkm.api.entities.lists.RetrievableRoleList
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

class MemberData(override val guild: Guild, override val data: JsonObject) : Member {

    val roleCache = Cache.fromSnowflakeEntityList(data.getValue("roles").jsonArray.map { guild.roles[Snowflake.fromId(it.jsonPrimitive.long)]!! })

    override var voiceState: VoiceState? = null

    override val roles: RetrievableRoleList
        get() = RetrievableRoleList(this, roleCache.values)

    override fun toString() = "Member[nickname=$nickname, id=$id]"

    override fun equals(other: Any?): Boolean {
        if(other !is Member) return false
        return other.id == id
    }

}

class VoiceStateData(override val client: Client, override val data: JsonObject) : VoiceState