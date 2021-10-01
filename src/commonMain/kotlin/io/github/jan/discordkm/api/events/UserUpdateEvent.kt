package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.Client

/**
 * Sent when the bot was updated
 */
class SelfUserUpdateEvent(val user: User, val oldUser: User?) : Event {

    override val client: Client
        get() = user.client

}