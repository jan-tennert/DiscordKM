/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild.invites

import io.github.jan.discordkm.api.entities.SerializableEntity
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.internal.entities.DiscordImage
import io.github.jan.discordkm.internal.utils.getId
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray

class InviteApplication(override val client: DiscordClient, override val data: JsonObject) : SerializableEntity {

    val id = data.getId()

    val name = data.getOrThrow<String>("name")

    val iconUrl = if(data.getOrNull<String>("icon") != null) DiscordImage.applicationImage(id, data.getOrNull<String>("icon")!!) else null

    val coverImageUrl = if(data.getOrNull<String>("cover_image") != null) DiscordImage.applicationImage(id, data.getOrNull<String>("cover_image")!!) else null

    val description = data.getOrThrow<String>("description")

    val rpcOrigins = data["rpc_origins"]?.jsonArray?.map { it.toString() } ?: emptyList()

    val termsOfServiceUrl = data.getOrNull<String>("terms_of_service_url")

    val privacyPolicyUrl = data.getOrNull<String>("privacy_policy_url")

    val summary = data.getOrThrow<String>("summary")

    val verifyKey = data.getOrThrow<String>("verify_key")

    //TODO: add remaining parameters
    override fun toString(): String = "InviteApplication(id=$id, name=$name)"
    override fun equals(other: Any?): Boolean = other is InviteApplication && other.id == id
    override fun hashCode(): Int = id.hashCode()

}
