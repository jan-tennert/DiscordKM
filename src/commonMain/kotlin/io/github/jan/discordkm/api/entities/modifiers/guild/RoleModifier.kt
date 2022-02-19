package io.github.jan.discordkm.api.entities.modifiers.guild

import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.misc.Color
import io.github.jan.discordkm.api.entities.modifiers.JsonModifier
import io.github.jan.discordkm.api.media.Image
import io.github.jan.discordkm.internal.serialization.rawValue
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

class RoleModifier : JsonModifier {

    var name: String? = null
    var permissions: MutableSet<Permission> = mutableSetOf()
    var color: Color? = null
    var hoist: Boolean? = null
    var mentionable: Boolean? = null
    var icon: Image? = null

    override val data: JsonObject
        get() = buildJsonObject {
            putOptional("name", name)
            putOptional("permissions", permissions.rawValue())
            putOptional("color", color?.rgb)
            putOptional("hoist", hoist)
            putOptional("icon", icon?.encodedData)
            putOptional("mentionable", mentionable)
        }

}
