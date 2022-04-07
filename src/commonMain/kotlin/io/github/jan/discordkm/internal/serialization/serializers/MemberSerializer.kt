package io.github.jan.discordkm.internal.serialization.serializers

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.member.MemberCacheEntry
import io.github.jan.discordkm.api.entities.guild.member.MemberCacheEntryImpl
import io.github.jan.discordkm.api.entities.guild.role.Role
import io.github.jan.discordkm.internal.serialization.GuildEntitySerializer
import io.github.jan.discordkm.internal.utils.boolean
import io.github.jan.discordkm.internal.utils.isoTimestamp
import io.github.jan.discordkm.internal.utils.snowflake
import io.github.jan.discordkm.internal.utils.string
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import io.github.jan.discordkm.internal.utils.get


object MemberSerializer : GuildEntitySerializer<MemberCacheEntry> {

    override fun deserialize(data: JsonObject, value: Guild) : MemberCacheEntry {
        val user = User(data["user"]!!.jsonObject, value.client)
        val cacheMember = value.cache?.members?.get(user.id)
        return MemberCacheEntryImpl(
            guild = value,
            user = user,
            nickname = data["nick", true]?.string,
            joinedAt = data["joined_at"]!!.isoTimestamp,
            isDeafened = data["deaf"]?.boolean ?: cacheMember?.isDeafened ?: false,
            isMuted = data["mute"]?.boolean ?: cacheMember?.isMuted ?: false,
            id = user.id,
            avatarHash = data["avatar", true]?.string,
            premiumSince = data["premium_since", true]?.isoTimestamp,
            isPending = data["pending", true]?.boolean ?: false,
            timeoutUntil = data["communication_disabled_until", true]?.isoTimestamp
        ).apply {
            cacheManager.roleCache.putAll(data["roles"]!!.jsonArray.map { Role(it.snowflake, value) }.associateBy { it.id })
        }
    }

}