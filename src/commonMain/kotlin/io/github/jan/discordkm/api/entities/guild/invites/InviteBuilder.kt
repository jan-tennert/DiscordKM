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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

class InviteBuilder(
    var maxAge: Int? = null,
    var maxUses: Int? = null,
    var isTemporary: Boolean? = null,
    var isUnique: Boolean? = null,
    var target: Target? = null
) {

    fun build() = InviteBuilderObject(maxAge, maxUses, isTemporary, isUnique, target?.type?.ordinal?.plus(1), if(target?.type == TargetType.STREAM) target?.targetId else null, if(target?.type == TargetType.EMBEDDED_APPLICATION) target?.targetId else null)

}