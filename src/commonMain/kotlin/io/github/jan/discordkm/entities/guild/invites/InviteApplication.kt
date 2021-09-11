package io.github.jan.discordkm.entities.guild.invites

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.SerializableEntity
import io.github.jan.discordkm.utils.DiscordImage
import io.github.jan.discordkm.utils.getId
import io.github.jan.discordkm.utils.getOrNull
import io.github.jan.discordkm.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray

data class InviteApplication(override val client: Client, override val data: JsonObject) : SerializableEntity {

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

}
