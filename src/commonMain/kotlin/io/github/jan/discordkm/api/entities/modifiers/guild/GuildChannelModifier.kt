package io.github.jan.discordkm.api.entities.modifiers.guild

import io.github.jan.discordkm.api.entities.PermissionHolder
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite
import io.github.jan.discordkm.api.entities.modifiers.JsonModifier
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray

sealed class GuildChannelModifier : JsonModifier {

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

    fun permissionOverwrite(holderId: Snowflake, type: PermissionOverwrite.HolderType, init: PermissionOverwriteBuilder.() -> Unit) {
        val builder = PermissionOverwriteBuilder(holderId, type)
        builder.init()
        permissionOverrides.add(builder.build())
    }

    fun permissionOverwrite(holder: PermissionHolder, init: PermissionOverwriteBuilder.() -> Unit) = permissionOverwrite(holder.id, holder.type, init)

    fun addPermissionOverride(holderId: Snowflake, holderType: PermissionOverwrite.HolderType, allow: Set<Permission> = emptySet(), deny: Set<Permission> = emptySet()) {
        permissionOverrides += PermissionOverwrite(holderId, allow.toMutableSet(), deny.toMutableSet(), holderType)
    }

    fun addPermissionOverride(holder: PermissionHolder, allow: Set<Permission> = emptySet(), deny: Set<Permission> = emptySet()) = addPermissionOverride(holder.id, holder.type, allow, deny)

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

class PermissionOverwriteBuilder(private val holderId: Snowflake, private val type: PermissionOverwrite.HolderType) {

    val allow = mutableSetOf<Permission>()
    val deny = mutableSetOf<Permission>()

    fun allow(vararg permissions: Permission) {
        allow.addAll(permissions)
    }

    fun deny(vararg permissions: Permission) {
        deny.addAll(permissions)
    }

    fun allow(permissions: Iterable<Permission>) {
        allow.addAll(permissions)
    }

    fun deny(permissions: Iterable<Permission>) {
        deny.addAll(permissions)
    }

    fun allow(permissions: MutableSet<Permission>.() -> Unit) {
        allow(buildSet(permissions))
    }

    fun build(): PermissionOverwrite {
        return PermissionOverwrite(holderId, allow.toMutableSet(), deny.toMutableSet(), type)
    }

    operator fun Permission.unaryPlus() {
        allow(this)
    }

    operator fun Permission.unaryMinus() {
        deny(this)
    }

}