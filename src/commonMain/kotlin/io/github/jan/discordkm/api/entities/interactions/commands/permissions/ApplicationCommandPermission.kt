package io.github.jan.discordkm.api.entities.interactions.commands.permissions

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.GuildEntity
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter

interface ApplicationCommandPermissions : GuildEntity {

    val commandId: Snowflake
    val applicationId: Snowflake
    val permissions: List<ApplicationCommandPermission>

}

sealed interface ApplicationCommandPermission: SnowflakeEntity {

    /**
     * The id of the role, user or channel. Can also be the guild id for the @everyone role and guild id - 1 for all channels
     */
    override val id: Snowflake

    /**
     * The type of the [ApplicationCommandPermission]
     */
    val type: ApplicationCommandPermissionType

    /**
     * Whether the permission is set to allowed
     */
    val isAllowed: Boolean

    enum class ApplicationCommandPermissionType : EnumWithValue<Int> {
        ROLE,
        USER,
        CHANNEL;

        override val value: Int
            get() = ordinal + 1

        companion object : EnumWithValueGetter<ApplicationCommandPermissionType, Int>(values())
    }

}

internal class ApplicationCommandPermissionsImpl(
    override val applicationId: Snowflake,
    override val permissions: List<ApplicationCommandPermission>,
    override val guild: Guild,
    override val commandId: Snowflake
) : ApplicationCommandPermissions {

    override fun equals(other: Any?) = other is ApplicationCommandPermissions && other.applicationId == applicationId && other.commandId == commandId
    override fun hashCode(): Int {
        var result = applicationId.hashCode()
        result = 31 * result + commandId.hashCode()
        return result
    }

    override fun toString() = "ApplicationCommandPermissions(applicationId=$applicationId, commandId=$commandId, permissions=$permissions, guildId=${guild.id})"


}

internal class ApplicationCommandPermissionImpl(
    override val id: Snowflake,
    override val type: ApplicationCommandPermission.ApplicationCommandPermissionType,
    override val isAllowed: Boolean
) : ApplicationCommandPermission {

    override fun equals(other: Any?) = other is ApplicationCommandPermission && other.id == id
    override fun hashCode() = id.hashCode()
    override fun toString() = "ApplicationCommandPermission(id=$id, type=$type, isAllowed=$isAllowed)"

}