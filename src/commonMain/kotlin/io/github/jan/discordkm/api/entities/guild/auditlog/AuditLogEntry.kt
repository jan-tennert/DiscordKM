/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild.auditlog

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/**
 * @param targetId The id of the affected entity (role, member, etc...)
 * @param changes The changes made to [targetId]
 * @param userId The user who made these changes
 * @param id The id of the entry
 * @param type The [AuditLogAction] that occurred
 * @param options Optional info for this audit log entry
 * @param reason The reason for this change
 */
@Serializable
class AuditLogEntry(
    @SerialName("target_id")
    val targetId: Snowflake? = null,
    @SerialName("user_id")
    val userId: Snowflake,
    override val id: Snowflake,
    @SerialName("action_type")
    val type: AuditLogAction,
    val reason: String? = null,
    val changes: List<AuditLogChange> = emptyList(),
    val options: AuditLogInfo? = null
) : SnowflakeEntity

/**
 * @param key The [audit log change](https://discord.com/developers/docs/resources/audit-log#audit-log-change-object-audit-log-change-key) key
 */
@Serializable
class AuditLogChange(
    @SerialName("new_value")
    val _newValue: JsonElement? = null,
    @SerialName("old_value")
    val _oldValue: JsonElement? = null,
    val key: String
) {

    /**
     * Can be a [Snowflake] a boolean, an int, a string, a [PermissionOverwrite] or a pair of role name and id
     */
    @Transient
    val newValue: Any? = if(key != "permission_overrides" && key != "\$add" && key != "\$remove") {
        _newValue?.jsonPrimitive?.booleanOrNull ?: _newValue?.jsonPrimitive?.intOrNull ?: _newValue?.jsonPrimitive?.doubleOrNull ?:  _newValue?.jsonPrimitive?.longOrNull ?: _newValue?.jsonPrimitive?.floatOrNull ?: _newValue?.jsonPrimitive?.content
    } else {
        if(key == "\$add" || key == "\$remove") {
            _newValue?.jsonArray?.map { it.jsonObject.getOrThrow<String>("name") to it.jsonObject.getOrThrow<Snowflake>("id") }
        } else {
            null
        }
    }

    /**
     * Can be a [Snowflake] a boolean, an int, a string, a [PermissionOverwrite] or a pair of role name and id
     */
    @Transient
    val oldValue: Any? = if(key != "permission_overrides" && key != "\$add" && key != "\$remove") {
        _oldValue?.jsonPrimitive?.booleanOrNull ?: _oldValue?.jsonPrimitive?.intOrNull ?: _oldValue?.jsonPrimitive?.doubleOrNull ?:  _oldValue?.jsonPrimitive?.longOrNull ?: _oldValue?.jsonPrimitive?.floatOrNull ?: _oldValue?.jsonPrimitive?.content
    } else {
        if(key == "\$add" || key == "\$remove") {
            _oldValue?.jsonArray?.map { it.jsonObject.getOrThrow<String>("name") to it.jsonObject.getOrThrow<Snowflake>("id") }
        } else {
            null
        }
    }

}

/**
 * @param channelId The channel id for member move, message (un)pin, message delete, and stage instance actions
 * @param count The amount of entities that were targeted (message (bulk) delete, member disconnect, member move)
 * @param deleteMemberDays Number of days after which inactive members were kicked (member prune)
 * @param id The id of the overridden entity (channel override actions)
 * @param membersRemoved The amount of member removed by the prune (member prune)
 * @param messageId The id of the message which was targeted (message (un)pin actions)
 * @param roleName The name of the role for channel override actions
 * @param type The type of the entity for channel override actions
 */
@Serializable
class AuditLogInfo(
    @SerialName("channel_id")
    val channelId: Snowflake? = null,
    val count: String? = null,
    @SerialName("delete_member_days")
    val deleteMemberDays: String? = null,
    val id: Snowflake? = null,
    @SerialName("members_removed")
    val membersRemoved: String? = null,
    @SerialName("message_id")
    val messageId: Snowflake? = null,
    @SerialName("role_name")
    val roleName: String? = null,
    val type: String? = null
)