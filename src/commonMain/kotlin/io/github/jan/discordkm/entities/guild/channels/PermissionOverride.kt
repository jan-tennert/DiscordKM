package io.github.jan.discordkm.entities.guild.channels

import io.github.jan.discordkm.entities.PermissionHolder
import io.github.jan.discordkm.entities.guild.Permission
import io.github.jan.discordkm.entities.misc.EnumList
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class PermissionOverride(val holder: PermissionHolder, val allow: EnumList<Permission> = EnumList.empty(), val deny: EnumList<Permission> = EnumList.empty()) {

    fun toJsonObject() = buildJsonObject {
        put("holder", holder.id.long)
        put("allow", allow.rawValue)
        put("deny", deny.rawValue)
    }

}