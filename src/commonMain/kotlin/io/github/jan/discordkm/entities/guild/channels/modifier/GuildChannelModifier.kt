package io.github.jan.discordkm.entities.guild.channels.modifier

import io.github.jan.discordkm.entities.PermissionHolder
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.guild.Permission
import io.github.jan.discordkm.entities.guild.channels.Category
import io.github.jan.discordkm.entities.guild.channels.GuildChannel
import io.github.jan.discordkm.entities.guild.channels.PermissionOverride
import io.github.jan.discordkm.entities.misc.EnumList
import io.github.jan.discordkm.utils.putJsonObject
import io.github.jan.discordkm.utils.putOptional
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray

sealed interface GuildChannelModifier <C : GuildChannel> {
    var name: String?
    val position: Int?
    val permissionOverrides: MutableList<PermissionOverride>

    fun addPermissionOverride(holder: PermissionHolder, allow: Set<Permission> = emptySet(), deny: Set<Permission> = emptySet()) {
        permissionOverrides += PermissionOverride(holder, EnumList(Permission, allow.toList()), EnumList(Permission, deny.toList()))
    }

    fun build() = buildJsonObject {
        putOptional("name", name)
        putOptional("position", position)
        putJsonArray("permission_overrides") {
            permissionOverrides.forEach {
                add(it.toJsonObject())
            }
        }
    }

}

sealed interface NonCategoryModifier <C : GuildChannel> : GuildChannelModifier<C> {

    var parentId: Snowflake?

    fun setParent(id: Snowflake) { parentId = id }

    fun setParent(category: Category) = setParent(category.id)

    override fun build() = buildJsonObject {
        putOptional("parent_id", parentId)
        putJsonObject(super.build())
    }

}