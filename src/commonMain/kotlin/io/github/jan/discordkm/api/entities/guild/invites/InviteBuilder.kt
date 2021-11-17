/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild.invites

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.invites.Invite.TargetType
import io.github.jan.discordkm.api.entities.modifiers.BaseModifier
import io.github.jan.discordkm.internal.check
import io.github.jan.discordkm.internal.utils.checkAndReturn
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Represents an invite target.
 * Use [TargetType] to create one
 */
data class Target internal constructor(val targetId: Snowflake, val type: TargetType)

@Serializable
data class InviteBuilderObject internal constructor(
    @SerialName("max_age")
    var maxAge: Int? = null,
    @SerialName("max_uses")
    var maxUses: Int? = null,
    @SerialName("temporary")
    var isTemporary: Boolean? = null,
    @SerialName("unique")
    var isUnique: Boolean? = null,
    @SerialName("target_type")
    var targetType: Int? = null,
    @SerialName("target_user_id")
    var targetUserId: Snowflake? = null,
    @SerialName("target_application_id")
    var targetApplicationId: Snowflake? = null
)

/**
 * @param maxAge The duration of invite in seconds before expire.
 *               (0-604800). 0 means never
 * @param maxUses Max number of uses (0-100). 0 means unlimited
 * @param isTemporary "Whether this invite only grants temporary membership"
 * @param isUnique "If true, don't try to reuse a similar invite (useful for creating many unique one time use invites)"
 * @param target The target of this invite. Can refer to a EmbeddedApplication or a user's stream
 */
class InviteBuilder : BaseModifier {

    var maxAge: Int? = null
    var maxUses: Int? = null
    var isTemporary: Boolean? = null
    var isUnique: Boolean? = null
    var target: Target? = null

    override val data: JsonObject
        get() = checkAndReturn {
            maxAge.check("The maximum age of an invite has to be between 0 and 604800 seconds") { it in 0..604799 }
            maxUses.check("The maximum uses of an invite have to be between 0 and 100") { it in 0..100 }
            buildJsonObject {
                putOptional("max_age", maxAge)
                putOptional("max_uses", maxUses)
                putOptional("unique", isUnique)
                putOptional("temporary", isTemporary)
                putOptional("target_type", target?.type?.value)
                if (target?.type == TargetType.STREAM) put("target_user_id", target!!.targetId.string)
                if (target?.type == TargetType.EMBEDDED_APPLICATION) put("target_application_id", target!!.targetId.string)
            }
        }

}