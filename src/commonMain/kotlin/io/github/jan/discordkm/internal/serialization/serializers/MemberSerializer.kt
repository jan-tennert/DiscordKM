package io.github.jan.discordkm.internal.serialization.serializers

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.containers.CacheMemberRoleContainer
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.MemberCacheEntry
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.internal.serialization.GuildEntitySerializer
import io.github.jan.discordkm.internal.utils.boolean
import io.github.jan.discordkm.internal.utils.isoTimestamp
import io.github.jan.discordkm.internal.utils.long
import io.github.jan.discordkm.internal.utils.snowflake
import io.github.jan.discordkm.internal.utils.string
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

object MemberSerializer : GuildEntitySerializer<MemberCacheEntry> {

    override fun deserialize(data: JsonObject, value: Guild) : MemberCacheEntry {
        val user = User.from(data["user"]!!.jsonObject, value.client)
        return MemberCacheEntry(
            guild = value,
            user = user,
            nickname = data["nick"]?.string,
            joinedAt = data["joined_at"]!!.isoTimestamp,
            isDeafened = data["deaf"]!!.boolean,
            isMuted = data["mute"]!!.boolean,
            id = user.id,
            avatarHash = data["avatar"]?.string,
            premiumSince = data["premium_since"]?.isoTimestamp,
            isPending = data["pending"]?.boolean ?: false
            //permissions?
        ).apply {
            cacheManager.roleCache.putAll(data["roles"]!!.jsonArray.map { Role.from(it.snowflake, value) }.associateBy { it.id })
        }
    }

}