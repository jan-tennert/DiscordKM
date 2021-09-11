package com.github.jan.discordkm.entities.channels

import com.github.jan.discordkm.Client
import com.github.jan.discordkm.entities.User
import com.github.jan.discordkm.utils.extractClientEntity
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class PrivateChannel(override val client: Client, override val data: JsonObject) : MessageChannel {

    val recipients = data.getValue("recipients").jsonArray.map { it.jsonObject.extractClientEntity<User>(client) }

}