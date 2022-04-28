package io.github.jan.discordkm.internal.serialization.serializers

import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.interactions.commands.permissions.ApplicationCommandPermission
import io.github.jan.discordkm.api.entities.interactions.commands.permissions.ApplicationCommandPermissionImpl
import io.github.jan.discordkm.api.entities.interactions.commands.permissions.ApplicationCommandPermissions
import io.github.jan.discordkm.api.entities.interactions.commands.permissions.ApplicationCommandPermissionsImpl
import io.github.jan.discordkm.internal.serialization.BaseEntitySerializer
import io.github.jan.discordkm.internal.utils.boolean
import io.github.jan.discordkm.internal.utils.getId
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.snowflake
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

object ApplicationCommandPermissionSerializer : BaseEntitySerializer<ApplicationCommandPermissions> {

    override fun deserialize(data: JsonObject, value: DiscordClient): ApplicationCommandPermissions {
        val id = data.getId()
        val applicationId = data["application_id"]!!.snowflake
        val guild = Guild(data["guild_id"]!!.snowflake, value)
        val permissions = data["permissions"]!!.jsonArray.map {
            val permissionId = it.jsonObject.getId()
            val type = ApplicationCommandPermission.ApplicationCommandPermissionType[it.jsonObject["type"]!!.int]
            val isAllowed = it.jsonObject["permission"]!!.boolean
            ApplicationCommandPermissionImpl(permissionId, type, isAllowed)
        }
        return ApplicationCommandPermissionsImpl(applicationId, permissions, guild, id)
    }

}