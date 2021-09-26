package io.github.jan.discordkm.internal.entities.guilds

import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.api.entities.guild.RoleModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.exceptions.PermissionException
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put

class RoleData(override val guild: Guild, override val data: JsonObject) : Role {

    override fun toString() = "Role[name=$name, id=$id]"

    override fun equals(other: Any?): Boolean {
        if(other !is Role) return false
        return other.id == id
    }

    override suspend fun modify(modifier: RoleModifier.() -> Unit) = client.buildRestAction<Role> {
        route = Route.Role.MODIFY_ROLE(guild.id, id).patch(RoleModifier().apply(modifier).build())
        transform { RoleData(guild, it.toJsonObject()) }
        onFinish { (guild as GuildData).roleCache[it.id] = it }
        check { if(Permission.MANAGE_ROLES !in guild.selfMember.permissions) throw PermissionException("You need the permission MANAGE_ROLES to modify a role") }
    }

    override suspend fun setPosition(newPosition: Int) = client.buildRestAction<Role> {
        route = Route.Role.MODIFY_ROLE_POSITION(guild.id).patch(buildJsonArray {
            addJsonObject {
                put("id", id.long)
                put("position", newPosition)
            }
        })
        transform { RoleData(guild, it.toJsonObject()) }
        onFinish { (guild as GuildData).roleCache[it.id] = it }
        check { if(Permission.MANAGE_ROLES !in guild.selfMember.permissions) throw PermissionException("You need the permission MANAGE_ROLES to modify the role position") }
    }

}