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
import io.github.jan.discordkm.api.entities.modifiers.JsonModifier
import io.github.jan.discordkm.internal.utils.putOptional
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Represents an invite target.
 * Use [TargetType] to create one
 */
data class InviteTarget internal constructor(val targetId: Snowflake, val type: TargetType)

class InviteBuilder : JsonModifier {

    /**
     * The duration of invite in seconds before expire.
     * (0-604800). 0 means never
     */
    var maxAge: Int? = null

    /**
     * Max number of uses (0-100). 0 means unlimited
     */
    var maxUses: Int? = null

    /**
     * "Whether this invite only grants temporary membership"
     */
    var isTemporary: Boolean? = null

    /**
     * "If true, don't try to reuse a similar invite (useful for creating many unique one time use invites)"
     */
    var isUnique: Boolean? = null

    /**
     * The target of this invite. Can refer to a EmbeddedApplication or a user's stream
     */
    var target: InviteTarget? = null

    override val data: JsonObject
        get() = buildJsonObject {
            putOptional("max_age", maxAge)
            putOptional("max_uses", maxUses)
            putOptional("unique", isUnique)
            putOptional("temporary", isTemporary)
            putOptional("target_type", target?.type?.value)
            if (target?.type == TargetType.STREAM) put("target_user_id", target!!.targetId.string)
            if (target?.type == TargetType.EMBEDDED_APPLICATION) put("target_application_id", target!!.targetId.string)
        }

}