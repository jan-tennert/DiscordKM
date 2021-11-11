package io.github.jan.discordkm.internal.serialization.serializers

import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.guild.RoleCacheEntry
import io.github.jan.discordkm.api.entities.misc.Color
import io.github.jan.discordkm.internal.serialization.BaseEntitySerializer
import io.github.jan.discordkm.internal.serialization.GuildEntitySerializer
import io.github.jan.discordkm.internal.utils.boolean
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.long
import io.github.jan.discordkm.internal.utils.snowflake
import io.github.jan.discordkm.internal.utils.string
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

object RoleSerializer : GuildEntitySerializer<RoleCacheEntry> {

    override fun deserialize(data: JsonObject, value: Guild) = RoleCacheEntry(
        id = data["id"]!!.snowflake,
        name = data["name"]!!.string,
        color = Color(data["color"]!!.int),
        isHoist = data["hoist"]!!.boolean,
        position = data["position"]!!.int,
        permissions = Permission.decode(data["position"]!!.long),
        isManagedByAnIntegration = data["managed"]!!.boolean,
        isMentionable = data["mentionable"]!!.boolean,
        guild = value,
        iconHash = data["icon"]?.string,
        unicodeEmoji = data["unicode_emoji"]?.string,
        tags = data["tags"]?.let { Json.decodeFromJsonElement(it) },
    )

}