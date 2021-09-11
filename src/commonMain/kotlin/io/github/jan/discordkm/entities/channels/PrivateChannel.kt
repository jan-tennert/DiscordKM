package io.github.jan.discordkm.entities.channels

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.User
import io.github.jan.discordkm.utils.extractClientEntity
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class PrivateChannel(override val client: Client, override val data: JsonObject) : MessageChannel {

    override val type = ChannelType.DM

    val recipients = data.getValue("recipients").jsonArray.map { it.jsonObject.extractClientEntity<User>(client) }

}