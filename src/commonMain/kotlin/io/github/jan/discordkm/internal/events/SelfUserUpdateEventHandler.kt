package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.events.SelfUserUpdateEvent
import io.github.jan.discordkm.internal.entities.UserData
import kotlinx.serialization.json.JsonObject

class SelfUserUpdateEventHandler(val client: Client) : InternalEventHandler<SelfUserUpdateEvent> {

    override fun handle(data: JsonObject): SelfUserUpdateEvent {
        val user = UserData(client, data)
        val oldUser = client.selfUser
        client.selfUser = user
        return SelfUserUpdateEvent(user, oldUser)
    }

}