package io.github.jan.discordkm.api.entities.modifiers.guild

import io.github.jan.discordkm.api.entities.PermissionHolder
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite
import io.github.jan.discordkm.api.entities.modifiers.BaseModifier
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray

sealed class GuildChannelModifier : BaseModifier {

    /**
     * The new name for the guild channel
     */
    var name: String? = null

    /**
     * The new position for the category
     */
    var position: Int? = null

    /**
     * The permissions for the category
     */
    val permissionOverrides: MutableList<PermissionOverwrite> = mutableListOf()

    fun addPermissionOverride(holder: PermissionHolder, allow: Set<Permission> = emptySet(), deny: Set<Permission> = emptySet()) {
        permissionOverrides += PermissionOverwrite(holder.id, allow.toMutableSet(), deny.toMutableSet(), holder.type)
    }

    override val data: JsonObject get() = buildJsonObject {
        putOptional("name", name)
        putOptional("position", position)
        putJsonArray("permission_overrides") {
            permissionOverrides.forEach {
                add(it.toJsonObject())
            }
        }
    }

}